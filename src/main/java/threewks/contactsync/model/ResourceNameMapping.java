package threewks.contactsync.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Maps Person API resource name an to internal resource ID
 */
@Entity
public class ResourceNameMapping {
    @Id
    private String resourceName;
    private String resourceId;
    private ZonedDateTime created;

    private ResourceNameMapping() {}

    public ResourceNameMapping(String resourceName, String resourceId) {
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.created = ZonedDateTime.now(Clock.systemUTC());
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceNameMapping that = (ResourceNameMapping) o;
        return Objects.equals(resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName);
    }
}
