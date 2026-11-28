/** A command line that is not in the expected form: unknown command, wrong fields, bad number, etc. */
public class InvalidInputException extends LibraryException {
    public InvalidInputException(String message) {
        super(message);
    }
}
