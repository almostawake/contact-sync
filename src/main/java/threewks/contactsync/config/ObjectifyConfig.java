package threewks.contactsync.config;

import contrib.springframework.data.gcp.objectify.config.ObjectifyConfigurer;
import org.springframework.context.annotation.Configuration;
import threewks.contactsync.model.ResourceNameMapping;
import threewks.contactsync.model.PersonSyncStatus;
import threewks.contactsync.model.PersonSyncLogEntry;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ObjectifyConfig implements ObjectifyConfigurer {
    @Override
    public List<Class<?>> registerObjectifyEntities() {
        return Arrays.asList(
            PersonSyncStatus.class,
            ResourceNameMapping.class,
            PersonSyncLogEntry.class
        );
    }
}
