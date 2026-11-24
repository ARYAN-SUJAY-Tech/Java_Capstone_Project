/** Links one member to one borrowed item, and knows its own due day and fine. */
public class Loan {
    private final int borrowDay;
    private final int dueDay;
    private final Member member;
    private final LibraryItem item;

    public Loan(Member member, LibraryItem item, int borrowDay) {
        this.member = member;
        this.item = item;
        this.borrowDay = borrowDay;
        this.dueDay = borrowDay + member.getLoanPeriod();
    }

    public Member getMember() { return member; }
    public LibraryItem getItem() { return item; }
    public int getBorrowDay() { return borrowDay; }
    public int getDueDay() { return dueDay; }

    public int daysLate(int today) {
        return Math.max(0, today - dueDay);
    }

    public int calculateFine(int today) {
        // Only Borrowable items are ever put on loan (checked in Library.borrow).
        return daysLate(today) * ((Borrowable) item).getFineRate();
    }
}
