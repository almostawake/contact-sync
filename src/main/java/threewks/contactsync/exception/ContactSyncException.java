package threewks.contactsync.exception;

public class ContactSyncException extends BaseException {
    public ContactSyncException(String format, Object... formatArgs) {
        super(format, formatArgs);
    }

    public ContactSyncException(Throwable cause, String format, Object... formatArgs) {
        super(cause, format, formatArgs);
    }
}
