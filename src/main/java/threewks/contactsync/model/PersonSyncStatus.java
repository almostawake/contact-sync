package threewks.contactsync.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.people.v1.model.Person;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.OnSave;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import threewks.contactsync.service.sync.Hasher;
import threewks.contactsync.exception.HashingException;
import threewks.contactsync.service.sync.SyncItem;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
public class PersonSyncStatus {

    private static ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
        .json()
        .featuresToEnable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();

    public static final String ID_FORMAT = "%s:%s";
    public static final Hasher PERSON_HASHER = (value) -> {
        // Strip unique data from person before hashing so we're comparing apples with apples.
        Person person = ((Person) value).clone()
            .setResourceName(null)
            .setEtag(null);

        String json;
        try {
            json = objectMapper.writeValueAsString(person);
        } catch (JsonProcessingException e) {
            throw new HashingException(e, "Error hashing person: {}", e.getMessage());
        }
        return DigestUtils.sha1Hex(json);
    };

    public static PersonSyncStatus create(String userId, String resourceId, Person a, Person b) {
        String compoundId = String.format(ID_FORMAT, userId, resourceId); // build a compound ID because there will be a status per person, per user
        return new PersonSyncStatus(compoundId, PERSON_HASHER.hash(a), PERSON_HASHER.hash(b));
    }

    public static Key<PersonSyncStatus> key(String userId, String resourceId) {
        return Key.create(PersonSyncStatus.class, String.format(ID_FORMAT, userId, resourceId));
    }

    @Id
    private String id;
    private String hashA;
    private String hashB;
    private ZonedDateTime created;
    private ZonedDateTime updated;

    private PersonSyncStatus() {}

    private PersonSyncStatus(String id, String hashA, String hashB) {
        this.id = id;
        this.hashA = hashA;
        this.hashB = hashB;
        this.created = ZonedDateTime.now(Clock.systemUTC());
    }

    public String getId() {
        return id;
    }

    public String getHashA() {
        return hashA;
    }

    public PersonSyncStatus setHashA(String hashA) {
        this.hashA = hashA;
        return this;
    }

    public String getHashB() {
        return hashB;
    }

    public PersonSyncStatus setHashB(String hashB) {
        this.hashB = hashB;
        return this;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonSyncStatus that = (PersonSyncStatus) o;
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
            .append("hashA", hashA)
            .append("hashB", hashB)
            .toString();
    }

    public SyncItem toSyncItem() {
        String resourceId = id.substring(id.indexOf(":") + 1); // SyncItem only needs the resource ID portion
        return new SyncItem(resourceId, hashA, hashB, PERSON_HASHER);
    }

    @OnSave
    private void updateTimestamp() {
        this.updated = ZonedDateTime.now(Clock.systemUTC());
    }
}
