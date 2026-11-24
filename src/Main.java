import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Reads commands from standard input and prints one result per command. */
public class Main {

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
        String[] f = line.split("\\|", -1);
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
                lines.add("OK today is day " + library.advance(Integer.parseInt(f[1])));
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
                // Input is guaranteed well-formed; ignore anything unknown.
                return lines;
        }

        // Reports with no rows print NONE; commands always add a line above.
        if (lines.isEmpty()) {
            lines.add("NONE");
        }
        return lines;
    }
}
