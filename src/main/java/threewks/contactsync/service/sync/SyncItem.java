package threewks.contactsync.service.sync;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

public class SyncItem {

    public static final Hasher DEFAULT_HASHER = Hasher.TO_STRING_SHA1_HASHER;

    public enum Status {
        NO_CHANGE,
        A_CHANGED,
        B_CHANGED,
        BOTH_CHANGED
    }

    public static <T> SyncItem create(String id, T a, T b) {
        return create(id, a, b, DEFAULT_HASHER);
    }

    public static <T> SyncItem create(String id, T a, T b, Hasher hasher) {
        return new SyncItem(id, hasher.hash(a), hasher.hash(b), hasher);
    }

    private final String id;
    private final String hashA;
    private final String hashB;
    private final Hasher hasher;

    public SyncItem(String id, String hashA, String hashB, Hasher hasher) {
        this.id = id;
        this.hashA = hashA;
        this.hashB = hashB;
        this.hasher = hasher;
    }

    public String getId() {
        return id;
    }

    public String getHashA() {
        return hashA;
    }

    public String getHashB() {
        return hashB;
    }

    public <T> SyncItem.Status checkStatus(T a, T b) {
        boolean leftChanged = !StringUtils.equals(hasher.hash(a), getHashA());
        boolean rightChanged = !StringUtils.equals(hasher.hash(b), getHashB());

        if (leftChanged && rightChanged) {
            return SyncItem.Status.BOTH_CHANGED;
        } else if (leftChanged) {
            return SyncItem.Status.A_CHANGED;
        } else if (rightChanged) {
            return SyncItem.Status.B_CHANGED;
        } else {
            return SyncItem.Status.NO_CHANGE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncItem syncItem = (SyncItem) o;
        return Objects.equals(id, syncItem.id) &&
            Objects.equals(hashA, syncItem.hashA) &&
            Objects.equals(hashB, syncItem.hashB) &&
            Objects.equals(hasher, syncItem.hasher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hashA, hashB, hasher);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("hashA", hashA)
            .append("hashB", hashB)
            .append("hasher", hasher)
            .toString();
    }
}
