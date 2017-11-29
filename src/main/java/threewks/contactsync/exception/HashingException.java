package threewks.contactsync.exception;

public class HashingException extends BaseException {
    public HashingException(String format, Object... formatArgs) {
        super(format, formatArgs);
    }

    public HashingException(Throwable cause, String format, Object... formatArgs) {
        super(cause, format, formatArgs);
    }
}
