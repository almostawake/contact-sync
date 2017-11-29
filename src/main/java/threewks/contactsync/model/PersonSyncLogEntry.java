package threewks.contactsync.model;

import com.google.api.services.people.v1.model.Person;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.apache.commons.lang3.builder.ToStringBuilder;
import threewks.contactsync.service.sync.SyncAction;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.util.UUID.randomUUID;

@Entity
public class PersonSyncLogEntry {
    @Id
    private String id;
    @Index
    private ZonedDateTime created;
    @Index
    private SyncAction.Type type;
    @Index
    private String resourceId;
    @Index
    private String userIdA;
    @Index
    private String userIdB;
    private String personA;
    private String personB;

    private PersonSyncLogEntry() {}

    public PersonSyncLogEntry(String userIdA, String userIdB, SyncAction<Person> action) {
        this.id = randomUUID().toString();
        this.created = ZonedDateTime.now(Clock.systemUTC());
        this.type = action.getType();
        this.resourceId = action.getId();
        this.userIdA = userIdA;
        this.userIdB = userIdB;
        this.personA = String.valueOf(action.getA());
        this.personB = String.valueOf(action.getB());
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public SyncAction.Type getType() {
        return type;
    }

    public String getUserIdA() {
        return userIdA;
    }

    public String getUserIdB() {
        return userIdB;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPersonA() {
        return personA;
    }

    public String getPersonB() {
        return personB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonSyncLogEntry that = (PersonSyncLogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("created", created)
            .append("type", type)
            .append("resourceId", resourceId)
            .append("userIdA", userIdA)
            .append("userIdB", userIdB)
            .append("personA", personA)
            .append("personB", personB)
            .toString();
    }
}
