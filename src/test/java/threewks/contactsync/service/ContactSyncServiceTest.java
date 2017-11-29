package threewks.contactsync.service;

import com.google.api.services.people.v1.model.Nickname;
import com.google.api.services.people.v1.model.Person;
import com.googlecode.objectify.Key;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import threewks.contactsync.model.PersonSyncLogEntry;
import threewks.contactsync.model.PersonSyncStatus;
import threewks.contactsync.model.ResourceNameMapping;
import threewks.contactsync.repository.PersonSyncLogEntryRepository;
import threewks.contactsync.repository.ResourceNameMappingRepository;
import threewks.contactsync.repository.SyncStatusRepository;
import threewks.contactsync.service.sync.SyncAction;
import threewks.contactsync.service.sync.SyncService;
import threewks.contactsync.testutil.ObjectifyTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static threewks.contactsync.testutil.TestData.generatePerson;
import static threewks.contactsync.testutil.TestData.generatePersonResourceName;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class ContactSyncServiceTest extends ObjectifyTest {

    private static final String MASTER_USER_ID = "master";
    private static final String USER_ID = "user";

    @Mock
    private PeopleService peopleService;
    @Mock
    private ResourceNameMappingRepository resourceNameMappingRepository;
    @Mock
    private SyncStatusRepository syncStatusRepository;
    @Mock
    private PersonSyncLogEntryRepository syncLogRepository;
    @Mock
    private GoogleApiAuthService googleApiAuthService;

    @Captor
    private ArgumentCaptor<ResourceNameMapping> resourceNameMappingArg;
    @Captor
    private ArgumentCaptor<PersonSyncStatus> personSyncStatusArg;
    @Captor
    private ArgumentCaptor<PersonSyncLogEntry> personSyncLogEntryArg;

    private ContactSyncService contactSyncService;

    @Before
    public void setUp() throws Exception {
        SyncService syncService = new SyncService();
        contactSyncService = new ContactSyncService(MASTER_USER_ID, peopleService, resourceNameMappingRepository, syncStatusRepository, syncLogRepository, syncService, googleApiAuthService);
    }

    @Test
    public void sync_willCreateContactForMaster_whenContactExistsOnlyForUser() throws Exception {
        Person existingPerson = generatePerson();
        String newResourceName = generatePersonResourceName();

        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(emptyList());
        when(peopleService.fetchPeople(USER_ID)).thenReturn(singletonList(existingPerson));
        when(peopleService.createPerson(anyString(), any(Person.class))).thenReturn(newResourceName);

        contactSyncService.syncUser(USER_ID);

        Person newPerson = existingPerson.clone().setResourceName(null);
        verify(peopleService).createPerson(MASTER_USER_ID, newPerson);

        String resourceId = verifyResourceMappingsUpdated(existingPerson.getResourceName(), newResourceName);

        verifySyncStatusUpdated(resourceId, newPerson, existingPerson);

        verifySyncLogEntryCreated(resourceId, SyncAction.Type.CREATE_A);
    }

    @Test
    public void sync_willCreateContactForUser_whenContactExistsOnlyForMaster() throws Exception {
        Person existingPerson = generatePerson();
        String newResourceName = generatePersonResourceName();

        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(singletonList(existingPerson));
        when(peopleService.fetchPeople(USER_ID)).thenReturn(emptyList());
        when(peopleService.createPerson(anyString(), any(Person.class))).thenReturn(newResourceName);

        contactSyncService.syncUser(USER_ID);

        Person newPerson = existingPerson.clone().setResourceName(null);
        verify(peopleService).createPerson(USER_ID, newPerson);

        String resourceId = verifyResourceMappingsUpdated(existingPerson.getResourceName(), newResourceName);

        verifySyncStatusUpdated(resourceId, existingPerson, newPerson);

        verifySyncLogEntryCreated(resourceId, SyncAction.Type.CREATE_B);
    }

    @Test
    public void sync_willUpdateMasterContact_whenUserContactUpdated() throws Exception {
        String resourceId = randomUUID().toString();
        Person masterPerson = generatePerson();
        Person userPerson = masterPerson.clone()
            .setResourceName(generatePersonResourceName())
            .setNicknames(Arrays.asList(new Nickname().setValue("Userish")));

        // create resource mappings since we'd already know about these resources
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, masterPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(masterPerson.getResourceName(), resourceId)));
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, userPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(userPerson.getResourceName(), resourceId)));

        // create a status which has hashes of the unchanged person resource
        mockPersonSyncStatus(resourceId, masterPerson);

        // ensure we get back different records when we fetch
        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(singletonList(masterPerson));
        when(peopleService.fetchPeople(USER_ID)).thenReturn(singletonList(userPerson));

        contactSyncService.syncUser(USER_ID);

        Person updatedMasterPerson = userPerson.clone().setResourceName(masterPerson.getResourceName());
        verify(peopleService).updatePerson(MASTER_USER_ID, updatedMasterPerson);

        verifySyncStatusUpdated(resourceId, updatedMasterPerson, userPerson);

        verifySyncLogEntryCreated(resourceId, SyncAction.Type.UPDATE_A);
    }

    @Test
    public void sync_willUpdateUserContact_whenMasterContactUpdated() throws Exception {
        String resourceId = randomUUID().toString();
        Person userPerson = generatePerson();
        Person masterPerson = userPerson.clone()
            .setResourceName(generatePersonResourceName())
            .setNicknames(Arrays.asList(new Nickname().setValue("Masterful")));

        // create resource mappings since we'd already know about these resources
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, masterPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(masterPerson.getResourceName(), resourceId)));
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, userPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(userPerson.getResourceName(), resourceId)));

        // create a status which has hashes of the unchanged person resource
        mockPersonSyncStatus(resourceId, userPerson);

        // ensure we get back different records when we fetch
        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(singletonList(masterPerson));
        when(peopleService.fetchPeople(USER_ID)).thenReturn(singletonList(userPerson));

        contactSyncService.syncUser(USER_ID);

        Person updatedUserPerson = masterPerson.clone().setResourceName(userPerson.getResourceName());
        verify(peopleService).updatePerson(USER_ID, updatedUserPerson);

        verifySyncStatusUpdated(resourceId, masterPerson, updatedUserPerson);

        verifySyncLogEntryCreated(resourceId, SyncAction.Type.UPDATE_B);
    }

    @Test
    public void sync_willFlagConflict_whenUserAndMasterUpdated() throws Exception {
        String resourceId = randomUUID().toString();
        Person originalPerson = generatePerson();
        Person userPerson = originalPerson.clone()
            .setResourceName(generatePersonResourceName())
            .setNicknames(Arrays.asList(new Nickname().setValue("Userish")));
        Person masterPerson = originalPerson.clone()
            .setResourceName(generatePersonResourceName())
            .setNicknames(Arrays.asList(new Nickname().setValue("Masterful")));

        // create resource mappings since we'd already know about these resources
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, masterPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(masterPerson.getResourceName(), resourceId)));
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, userPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(userPerson.getResourceName(), resourceId)));

        // create a status which has hashes of the unchanged person resource
        mockPersonSyncStatus(resourceId, originalPerson);

        // ensure we get back different records when we fetch
        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(singletonList(masterPerson));
        when(peopleService.fetchPeople(USER_ID)).thenReturn(singletonList(userPerson));

        contactSyncService.syncUser(USER_ID);

        verifySyncLogEntryCreated(resourceId, SyncAction.Type.CONFLICT);
    }

    @Test
    public void sync_willDeleteMasterContact_whenUserContactDeleted() throws Exception {
        String resourceId = randomUUID().toString();
        Person masterPerson = generatePerson();

        // create resource mappings since we'd already know about these resources
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, masterPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(masterPerson.getResourceName(), resourceId)));

        // create a status tracking the person resource
        PersonSyncStatus status = mockPersonSyncStatus(resourceId, masterPerson);

        // ensure we get back different records when we fetch
        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(singletonList(masterPerson));
        when(peopleService.fetchPeople(USER_ID)).thenReturn(emptyList());

        contactSyncService.syncUser(USER_ID);

        verify(peopleService).deletePerson(MASTER_USER_ID, masterPerson);
        verify(syncStatusRepository).delete(status);
        verifySyncLogEntryCreated(resourceId, SyncAction.Type.DELETE_A);
    }

    @Test
    public void sync_willDeleteUserContact_whenMasterContactDeleted() throws Exception {
        String resourceId = randomUUID().toString();
        Person userPerson = generatePerson();

        // create resource mappings since we'd already know about these resources
        when(resourceNameMappingRepository.findAll(singletonList(Key.create(ResourceNameMapping.class, userPerson.getResourceName()))))
            .thenReturn(singletonList(new ResourceNameMapping(userPerson.getResourceName(), resourceId)));

        // create a status tracking the person resource
        PersonSyncStatus status = mockPersonSyncStatus(resourceId, userPerson);

        // ensure we get back different records when we fetch
        when(peopleService.fetchPeople(MASTER_USER_ID)).thenReturn(emptyList());
        when(peopleService.fetchPeople(USER_ID)).thenReturn(singletonList(userPerson));

        contactSyncService.syncUser(USER_ID);

        verify(peopleService).deletePerson(USER_ID, userPerson);
        verify(syncStatusRepository).delete(status);
        verifySyncLogEntryCreated(resourceId, SyncAction.Type.DELETE_B);
    }

    private PersonSyncStatus mockPersonSyncStatus(String resourceId, Person person) {
        Key<PersonSyncStatus> statusKey = PersonSyncStatus.key(USER_ID, resourceId);
        PersonSyncStatus status = PersonSyncStatus.create(USER_ID, resourceId, person, person);
        when(syncStatusRepository.findAll(Sets.newLinkedHashSet(statusKey))).thenReturn(Arrays.asList(status));
        when(syncStatusRepository.findOne(statusKey)).thenReturn(Optional.of(status));
        return status;
    }

    private String verifyResourceMappingsUpdated(String existingResourceName, String newResourceName) {
        verify(resourceNameMappingRepository).save(resourceNameMappingArg.capture(), resourceNameMappingArg.capture());
        List<ResourceNameMapping> mappings = resourceNameMappingArg.getAllValues();
        assertThat(mappings, hasSize(2));

        ResourceNameMapping resourceNameMapping1 = mappings.get(0);
        assertThat(resourceNameMapping1.getResourceName(), is(existingResourceName));

        ResourceNameMapping resourceNameMapping2 = mappings.get(1);
        assertThat(resourceNameMapping2.getResourceName(), is(newResourceName));

        assertThat(resourceNameMapping1.getResourceId(), is(resourceNameMapping2.getResourceId()));

        return resourceNameMapping1.getResourceId();
    }

    private void verifySyncStatusUpdated(String resourceId, Person masterPerson, Person userPerson) {
        verify(syncStatusRepository).save(personSyncStatusArg.capture());
        PersonSyncStatus status = personSyncStatusArg.getValue();
        assertThat(status.getId(), is(String.format(PersonSyncStatus.ID_FORMAT, USER_ID, resourceId)));
        assertThat(status.getHashA(), is(PersonSyncStatus.PERSON_HASHER.hash(masterPerson)));
        assertThat(status.getHashB(), is(PersonSyncStatus.PERSON_HASHER.hash(userPerson)));
    }

    private void verifySyncLogEntryCreated(String resourceId, SyncAction.Type actionType) {
        verify(syncLogRepository).save(personSyncLogEntryArg.capture());
        PersonSyncLogEntry entry = personSyncLogEntryArg.getValue();
        assertThat(entry.getResourceId(), is(resourceId));
        assertThat(entry.getUserIdA(), is(MASTER_USER_ID));
        assertThat(entry.getUserIdB(), is(USER_ID));
        assertThat(entry.getType(), is(actionType));
    }
}
