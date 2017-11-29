package threewks.contactsync.exception;

public class PeopleApiException extends BaseException {
    public PeopleApiException(String format, Object... formatArgs) {
        super(format, formatArgs);
    }

    public PeopleApiException(Throwable cause, String format, Object... formatArgs) {
        super(cause, format, formatArgs);
    }
}
