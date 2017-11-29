package threewks.contactsync.repository;

import contrib.springframework.data.gcp.objectify.repository.ObjectifyRepository;
import org.springframework.stereotype.Repository;
import threewks.contactsync.model.PersonSyncLogEntry;

@Repository
public interface PersonSyncLogEntryRepository extends ObjectifyRepository<PersonSyncLogEntry, String> {
}
