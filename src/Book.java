public class Book extends LibraryItem implements Borrowable {
    private final String author;
    private final int fineRate = 2;

    public Book(String id, String title, String author) {
        super(id, title);
        this.author = author;
    }

    public String getAuthor() { return author; }

    @Override
    public int getFineRate() { return fineRate; }

    @Override
    public String getType() { return "BOOK"; }
}
