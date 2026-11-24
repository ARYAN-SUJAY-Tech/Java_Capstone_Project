public class Magazine extends LibraryItem implements Borrowable {
    private final String issue;
    private final int fineRate = 1;

    public Magazine(String id, String title, String issue) {
        super(id, title);
        this.issue = issue;
    }

    public String getIssue() { return issue; }

    @Override
    public int getFineRate() { return fineRate; }

    @Override
    public String getType() { return "MAGAZINE"; }
}
