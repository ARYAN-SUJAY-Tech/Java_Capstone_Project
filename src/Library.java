import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/** Holds the catalogue, the members, the active loans and today's day. */
public class Library {
    private int currentDay = 0;
    private final Map<String, LibraryItem> items = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    /** Active loans keyed by item ID: an item can be on loan to only one member. */
    private final Map<String, Loan> loans = new HashMap<>();
    /** Reservation queues keyed by item ID, holding member IDs in arrival order. */
    private final Map<String, Queue<String>> reservations = new HashMap<>();

    public int getCurrentDay() { return currentDay; }

    // ---------- ADD_ commands ----------

    public void addBook(String id, String title, String author) {
        items.put(id, new Book(id, title, author));
    }

    public void addMagazine(String id, String title, String issue) {
        items.put(id, new Magazine(id, title, issue));
    }

    public void addReference(String id, String title, String author) {
        items.put(id, new ReferenceBook(id, title, author));
    }

    /** The only place a member type string is looked at: it picks which class to create. */
    public void addMember(String id, String name, String type) {
        Member member;
        switch (type) {
            case "STUDENT": member = new Student(id, name); break;
            case "STAFF":   member = new Staff(id, name); break;
            case "FACULTY": member = new Faculty(id, name); break;
            default: throw new IllegalArgumentException("unknown member type " + type);
        }
        members.put(id, member);
    }

    // ---------- lookups ----------

    private LibraryItem findItem(String itemId) throws ItemNotFoundException {
        LibraryItem item = items.get(itemId);
        if (item == null) {
            throw new ItemNotFoundException("item " + itemId + " not found");
        }
        return item;
    }

    private Member findMember(String memberId) throws MemberNotFoundException {
        Member member = members.get(memberId);
        if (member == null) {
            throw new MemberNotFoundException("member " + memberId + " not found");
        }
        return member;
    }

    private int loanCount(Member member) {
        int count = 0;
        for (Loan loan : loans.values()) {
            if (loan.getMember() == member) {
                count++;
            }
        }
        return count;
    }

    private boolean isAtLimit(Member member) {
        return loanCount(member) >= member.getMaxItems();
    }

    // ---------- BORROW / RETURN / ADVANCE ----------

    /** Lends the item and returns its due day. */
    public int borrow(String memberId, String itemId) throws LibraryException {
        Member member = findMember(memberId);
        LibraryItem item = findItem(itemId);
        if (!item.isBorrowable()) {
            throw new ItemNotAvailableException(itemId + " cannot be borrowed");
        }
        if (loans.containsKey(itemId)) {
            throw new ItemNotAvailableException(itemId + " is on loan");
        }
        if (isAtLimit(member)) {
            throw new BorrowLimitExceededException(
                    memberId + " has reached the limit of " + member.getMaxItems());
        }
        Loan loan = issue(member, item);
        // A member who takes the item directly no longer needs to wait for it.
        Queue<String> queue = reservations.get(itemId);
        if (queue != null) {
            queue.remove(memberId);
        }
        return loan.getDueDay();
    }

    private Loan issue(Member member, LibraryItem item) {
        Loan loan = new Loan(member, item, currentDay);
        loans.put(item.getId(), loan);
        return loan;
    }

    /** Takes the item back and returns the text after "OK " for the result line. */
    public String returnItem(String itemId) throws LibraryException {
        findItem(itemId);
        Loan loan = loans.remove(itemId);
        if (loan == null) {
            throw new ItemNotOnLoanException(itemId + " is not on loan");
        }
        String result = "fine " + loan.calculateFine(currentDay);

        Loan next = issueToNextInQueue(loan.getItem());
        if (next != null) {
            result += ", issued to " + next.getMember().getId() + " due on day " + next.getDueDay();
        }
        return result;
    }

    /** Gives the item to the first queued member below their limit; members at their limit stay queued. */
    private Loan issueToNextInQueue(LibraryItem item) {
        Queue<String> queue = reservations.get(item.getId());
        if (queue == null) {
            return null;
        }
        Iterator<String> it = queue.iterator();
        while (it.hasNext()) {
            Member member = members.get(it.next());
            if (!isAtLimit(member)) {
                it.remove();
                return issue(member, item);
            }
        }
        return null;
    }

    /** Moves time forward and returns the new day. */
    public int advance(int days) {
        currentDay += days;
        return currentDay;
    }

    // ---------- RESERVE (bonus) ----------

    public void reserve(String memberId, String itemId) throws LibraryException {
        findMember(memberId);
        LibraryItem item = findItem(itemId);
        if (!item.isBorrowable()) {
            throw new ItemNotAvailableException(itemId + " cannot be borrowed");
        }
        Loan loan = loans.get(itemId);
        if (loan == null) {
            throw new ReservationNotAllowedException(itemId + " is available");
        }
        if (loan.getMember().getId().equals(memberId)) {
            throw new ReservationNotAllowedException(memberId + " already has " + itemId);
        }
        Queue<String> queue = reservations.computeIfAbsent(itemId, k -> new ArrayDeque<>());
        if (queue.contains(memberId)) {
            throw new ReservationNotAllowedException(memberId + " already reserved " + itemId);
        }
        queue.add(memberId);
    }

    // ---------- reports ----------

    /** Items whose title contains the text (case-insensitive), sorted by title then ID. */
    public List<LibraryItem> search(String text) {
        String needle = text.toLowerCase();
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            if (item.getTitle().toLowerCase().contains(needle)) {
                result.add(item);
            }
        }
        result.sort(Comparator.comparing(LibraryItem::getTitle)
                .thenComparing(LibraryItem::getId));
        return result;
    }

    /** A member's current loans, by due day then item ID. */
    public List<Loan> loans(String memberId) throws MemberNotFoundException {
        Member member = findMember(memberId);
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans.values()) {
            if (loan.getMember() == member) {
                result.add(loan);
            }
        }
        result.sort(Comparator.comparingInt(Loan::getDueDay)
                .thenComparing(loan -> loan.getItem().getId()));
        return result;
    }

    /** Loans past their due day, most days late first, then item ID. */
    public List<Loan> overdue() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans.values()) {
            if (loan.daysLate(currentDay) > 0) {
                result.add(loan);
            }
        }
        result.sort(Comparator.comparingInt((Loan loan) -> loan.daysLate(currentDay)).reversed()
                .thenComparing(loan -> loan.getItem().getId()));
        return result;
    }

    /** One line per item, "itemId|TYPE|status", sorted by item ID. */
    public List<String> inventory() {
        List<LibraryItem> sorted = new ArrayList<>(items.values());
        sorted.sort(Comparator.comparing(LibraryItem::getId));
        List<String> lines = new ArrayList<>();
        for (LibraryItem item : sorted) {
            String status;
            if (!item.isBorrowable()) {
                status = "NOT_BORROWABLE";
            } else if (loans.containsKey(item.getId())) {
                status = "ON_LOAN|" + loans.get(item.getId()).getMember().getId();
            } else {
                status = "AVAILABLE";
            }
            lines.add(item.getId() + "|" + item.getType() + "|" + status);
        }
        return lines;
    }
}
