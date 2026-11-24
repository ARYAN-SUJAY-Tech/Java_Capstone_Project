/** Parent of every rule failure; caught once in Main's command loop. */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
