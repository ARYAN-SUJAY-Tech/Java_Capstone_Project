import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads commands from standard input and prints one result per command. */
public class Main {

    /** The exact form of every command; also gives the number of fields each one needs. */
    private static final Map<String, String> USAGE = new LinkedHashMap<>();
    static {
        USAGE.put("ADD_BOOK", "ADD_BOOK|id|title|author");
        USAGE.put("ADD_MAGAZINE", "ADD_MAGAZINE|id|title|issue");
        USAGE.put("ADD_REFERENCE", "ADD_REFERENCE|id|title|author");
        USAGE.put("ADD_MEMBER", "ADD_MEMBER|id|name|type");
        USAGE.put("BORROW", "BORROW|memberId|itemId");
        USAGE.put("RETURN", "RETURN|itemId");
        USAGE.put("ADVANCE", "ADVANCE|days");
        USAGE.put("RESERVE", "RESERVE|memberId|itemId");
        USAGE.put("SEARCH", "SEARCH|text");
        USAGE.put("LOANS", "LOANS|memberId");
        USAGE.put("OVERDUE", "OVERDUE");
        USAGE.put("INVENTORY", "INVENTORY");
    }

    public static void main(String[] args) throws IOException {
        Library library = new Library();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            // The single place where every rule failure is caught.
            try {
                for (String result : execute(library, line.strip())) {
                    System.out.println(result);
                }
            } catch (LibraryException e) {
                System.out.println("ERROR " + e.getMessage());
            }
        }
    }

    /** Runs one command and returns the lines it prints. */
    private static List<String> execute(Library library, String line) throws LibraryException {
        String[] f = parse(line);
        List<String> lines = new ArrayList<>();

        switch (f[0]) {
            case "ADD_BOOK":
                library.addBook(f[1], f[2], f[3]);
                lines.add("OK");
                break;
            case "ADD_MAGAZINE":
                library.addMagazine(f[1], f[2], f[3]);
                lines.add("OK");
                break;
            case "ADD_REFERENCE":
                library.addReference(f[1], f[2], f[3]);
                lines.add("OK");
                break;
            case "ADD_MEMBER":
                library.addMember(f[1], f[2], f[3]);
                lines.add("OK");
                break;
            case "BORROW":
                lines.add("OK due on day " + library.borrow(f[1], f[2]));
                break;
            case "RETURN":
                lines.add("OK " + library.returnItem(f[1]));
                break;
            case "ADVANCE":
                lines.add("OK today is day " + library.advance(parseDays(f[1])));
                break;
            case "RESERVE":
                library.reserve(f[1], f[2]);
                lines.add("OK reserved");
                break;
            case "SEARCH":
                for (LibraryItem item : library.search(f[1])) {
                    lines.add(item.getId() + "|" + item.getType() + "|" + item.getTitle());
                }
                break;
            case "LOANS":
                for (Loan loan : library.loans(f[1])) {
                    lines.add(loan.getItem().getId() + "|" + loan.getItem().getTitle()
                            + "|" + loan.getDueDay());
                }
                break;
            case "OVERDUE":
                int today = library.getCurrentDay();
                for (Loan loan : library.overdue()) {
                    lines.add(loan.getItem().getId() + "|" + loan.getMember().getId()
                            + "|" + loan.daysLate(today) + "|" + loan.calculateFine(today));
                }
                break;
            case "INVENTORY":
                lines.addAll(library.inventory());
                break;
            default:
                // Unreachable: parse() already rejected unknown commands.
                throw new InvalidInputException("unknown command " + f[0]);
        }

        // Reports with no rows print NONE; commands always add a line above.
        if (lines.isEmpty()) {
            lines.add("NONE");
        }
        return lines;
    }

    /** Splits a line into trimmed fields and checks it matches its command's form. */
    private static String[] parse(String line) throws InvalidInputException {
        String[] f = line.split("\\|", -1);
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].strip();
        }
        if (f[0].isEmpty()) {
            throw new InvalidInputException("missing command");
        }
        String usage = USAGE.get(f[0]);
        if (usage == null) {
            throw new InvalidInputException("unknown command " + f[0]);
        }
        int expectedFields = usage.split("\\|").length;
        if (f.length != expectedFields) {
            throw new InvalidInputException("invalid format, expected " + usage);
        }
        for (String field : f) {
            if (field.isEmpty()) {
                throw new InvalidInputException("invalid format, expected " + usage);
            }
        }
        return f;
    }

    /** Days must be a whole number >= 0 (at most 9 digits, so it always fits in an int). */
    private static int parseDays(String text) throws InvalidInputException {
        if (!text.matches("\\d{1,9}")) {
            throw new InvalidInputException("invalid number of days " + text);
        }
        return Integer.parseInt(text);
    }
}
