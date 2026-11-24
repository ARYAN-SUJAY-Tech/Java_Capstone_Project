/** Base class for everything the library holds. */
public abstract class LibraryItem {
    private final String id;
    private final String title;

    public LibraryItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }

    /** BOOK, MAGAZINE or REFERENCE, as printed in reports. */
    public abstract String getType();

    /** Items decide whether they can be borrowed: only Borrowable items can. */
    public boolean isBorrowable() {
        return this instanceof Borrowable;
    }
}
