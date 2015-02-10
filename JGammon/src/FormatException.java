
/**
 * Wrong file format
 *
 * @author Aviv
 * @version 1.0
 */
public class FormatException extends Exception {
    public FormatException() {
        super();
    }

    public FormatException(String message) {
        super(message);
    }

    public FormatException(Throwable cause) {
        super(cause);
    }

    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
