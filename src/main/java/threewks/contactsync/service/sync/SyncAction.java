package threewks.contactsync.service.sync;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

public class SyncAction<T> {

    public enum Type {
        CREATE_A,
        CREATE_B,
        UPDATE_A,
        UPDATE_B,
        CONFLICT,
        DELETE_A,
        DELETE_B,
        STOP_TRACKING,
    }

    private final Type type;
    private final String id;
    private final T a;
    private final T b;

    public SyncAction(Type type, String id, T a, T b) {
        this.type = type;
        this.id = id;
        this.a = a;
        this.b = b;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public T getA() {
        return a;
    }

    public T getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncAction<?> that = (SyncAction<?>) o;
        return type == that.type &&
            Objects.equals(id, that.id) &&
            Objects.equals(a, that.a) &&
            Objects.equals(b, that.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, a, b);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("type", type)
            .append("id", id)
            .append("a", a)
            .append("b", b)
            .toString();
    }
}
