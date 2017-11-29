package threewks.contactsync.repository;

import contrib.springframework.data.gcp.objectify.repository.ObjectifyRepository;
import org.springframework.stereotype.Repository;
import threewks.contactsync.model.PersonSyncStatus;

@Repository
public interface SyncStatusRepository extends ObjectifyRepository<PersonSyncStatus, String> {
}
