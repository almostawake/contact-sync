package threewks.contactsync.service;

import com.google.api.services.people.v1.model.Person;
import com.googlecode.objectify.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import threewks.contactsync.exception.ContactSyncException;
import threewks.contactsync.model.PersonSyncLogEntry;
import threewks.contactsync.model.PersonSyncStatus;
import threewks.contactsync.model.ResourceNameMapping;
import threewks.contactsync.repository.PersonSyncLogEntryRepository;
import threewks.contactsync.repository.ResourceNameMappingRepository;
import threewks.contactsync.repository.SyncStatusRepository;
import threewks.contactsync.service.sync.SyncAction;
import threewks.contactsync.service.sync.SyncItem;
import threewks.contactsync.service.sync.SyncService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.UUID.randomUUID;

@Service
public class ContactSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(ContactSyncService.class);

    private final String masterUserId;
    private final PeopleService peopleService;
    private final ResourceNameMappingRepository resourceNameMappingRepository;
    private final SyncStatusRepository syncStatusRepository;
    private final PersonSyncLogEntryRepository syncLogRepository;
    private final SyncService syncService;
    private final GoogleApiAuthService googleApiAuthService;

    public ContactSyncService(@Value("${masterUserId}") String masterUserId,
                              PeopleService peopleService,
                              ResourceNameMappingRepository resourceNameMappingRepository,
                              SyncStatusRepository syncStatusRepository,
                              PersonSyncLogEntryRepository syncLogRepository,
                              SyncService syncService,
                              GoogleApiAuthService googleApiAuthService) {
        this.masterUserId = masterUserId;
        this.peopleService = peopleService;
        this.resourceNameMappingRepository = resourceNameMappingRepository;
        this.syncStatusRepository = syncStatusRepository;
        this.syncLogRepository = syncLogRepository;
        this.syncService = syncService;
        this.googleApiAuthService = googleApiAuthService;
    }

    public void syncUser(String userId) {
        LOG.info("Starting sync for {}...", userId);

        LOG.info("Fetching master contact list");
        Map<String, Person> masterContacts = mapResourceNamesToIds(peopleService.fetchPeople(masterUserId));

        LOG.info("Fetching {} contact list", userId);
        Map<String, Person> userContacts = mapResourceNamesToIds(peopleService.fetchPeople(userId));

        LOG.info("Loading sync statuses");
        Map<String, SyncItem> statuses = loadSyncStatuses(userId, masterContacts, userContacts);

        LOG.info("Generating sync actions...");
        List<SyncAction<Person>> actions = syncService.sync(masterContacts, userContacts, statuses);

        LOG.info("Processing {} sync actions", actions.size());
        process(userId, actions);

        LOG.info("Sync complete for {}", userId);
    }

    public void syncAll() {
        Set<String> keys;
        try {
            keys = googleApiAuthService.getStoredCredentialKeys();
        } catch (IOException e) {
            throw new ContactSyncException(e, "Error reading stored credentials: %s", e.getMessage());
        }

        LOG.info("Sync all users...");

        keys.stream().filter(userId -> !userId.equals(masterUserId)).forEach(this::syncUser);

        LOG.info("Sync completed for all users");
    }

    private Map<String, Person> mapResourceNamesToIds(List<Person> people) {
        List<Key<ResourceNameMapping>> mappingKeys = people
            .stream()
            .map(person -> Key.create(ResourceNameMapping.class, person.getResourceName()))
            .collect(Collectors.toList());

        Map<String, String> resourceNameMap = resourceNameMappingRepository
            .findAll(mappingKeys)
            .stream()
            .collect(
                Collectors.toMap(
                    ResourceNameMapping::getResourceName,
                    ResourceNameMapping::getResourceId
                )
            );

        return people.stream().collect(
            Collectors.toMap(
                person -> resourceNameMap.getOrDefault(person.getResourceName(), randomUUID().toString()),
                person -> person
            )
        );
    }

    private Map<String, SyncItem> loadSyncStatuses(String userId, Map<String, Person> masterContacts, Map<String, Person> userContacts) {
        Set<Key<PersonSyncStatus>> statusKeys = Stream
            .concat(masterContacts.keySet().stream(), userContacts.keySet().stream())
            .map(resourceId -> PersonSyncStatus.key(userId, resourceId))
            .collect(Collectors.toSet());

        return syncStatusRepository
            .findAll(statusKeys)
            .stream()
            .collect(Collectors.toMap(
                status -> status.toSyncItem().getId(),
                PersonSyncStatus::toSyncItem
            ));
    }

    private void process(String userId, List<SyncAction<Person>> actions) {

        for (SyncAction<Person> action : actions) {
            ofy().transact(() -> {
                String resourceId = action.getId();
                Person masterPerson = action.getA();
                Person userPerson = action.getB();

                switch (action.getType()) {
                    case CREATE_A:
                        resourceNameMappingRepository.save(
                            new ResourceNameMapping(userPerson.getResourceName(), resourceId),
                            new ResourceNameMapping(
                                peopleService.createPerson(
                                    masterUserId,
                                    userPerson.clone()
                                        .setResourceName(null)
                                        .setEtag(null)
                                ),
                                resourceId
                            )
                        );
                        syncStatusRepository.save(PersonSyncStatus.create(userId, resourceId, userPerson, userPerson));
                        break;
                    case CREATE_B:
                        resourceNameMappingRepository.save(
                            new ResourceNameMapping(masterPerson.getResourceName(), resourceId),
                            new ResourceNameMapping(
                                peopleService.createPerson(
                                    userId,
                                    masterPerson.clone()
                                        .setResourceName(null)
                                        .setEtag(null)
                                ),
                                resourceId
                            )
                        );
                        syncStatusRepository.save(PersonSyncStatus.create(userId, resourceId, masterPerson, masterPerson));
                        break;
                    case UPDATE_A:
                        peopleService.updatePerson(
                            masterUserId,
                            userPerson.clone()
                                .setResourceName(masterPerson.getResourceName())
                                .setEtag(masterPerson.getEtag())
                        );
                        syncStatusRepository.save(PersonSyncStatus.create(userId, resourceId, userPerson, userPerson));
                        break;
                    case UPDATE_B:
                        peopleService.updatePerson(
                            userId,
                            masterPerson.clone()
                                .setResourceName(userPerson.getResourceName())
                                .setEtag(userPerson.getEtag())
                        );
                        syncStatusRepository.save(PersonSyncStatus.create(userId, resourceId, masterPerson, masterPerson));
                        break;
                    case DELETE_A:
                        peopleService.deletePerson(masterUserId, masterPerson);
                        syncStatusRepository.findOne(PersonSyncStatus.key(userId, resourceId)).ifPresent(syncStatusRepository::delete);
                        break;
                    case DELETE_B:
                        peopleService.deletePerson(userId, userPerson);
                        syncStatusRepository.findOne(PersonSyncStatus.key(userId, resourceId)).ifPresent(syncStatusRepository::delete);
                        break;
                    case CONFLICT:
                        LOG.error("Both master and user contact {} have changed...waaaah!", resourceId);
                        break;
                }

                syncLogRepository.save(new PersonSyncLogEntry(masterUserId, userId, action));
            });
        }
    }
}
