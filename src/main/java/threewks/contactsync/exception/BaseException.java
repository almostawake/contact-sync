package threewks.contactsync.exception;

public class BaseException extends RuntimeException {
    public BaseException(String format, Object... formatArgs) {
        super(formatArgs.length == 0 ? format : String.format(format, formatArgs));
    }

    public BaseException(Throwable cause, String format, Object... formatArgs) {
        super(formatArgs.length == 0 ? format : String.format(format, formatArgs), cause);
    }
}
