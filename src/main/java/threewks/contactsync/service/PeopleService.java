package threewks.contactsync.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.json.GenericJson;
import com.google.api.services.people.v1.model.Biography;
import com.google.api.services.people.v1.model.FieldMetadata;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import threewks.contactsync.exception.PeopleApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High-level wrapper for the Google People API service client.
 */
@Service
public class PeopleService {
    private static final Logger LOG = LoggerFactory.getLogger(PeopleService.class);
    private static final String RESOURCE_NAME = "people/me";
    private static final List<String> WRITABLE_PERSON_FIELDS = Arrays.asList(
        "addresses",
        "biographies",
        "birthdays",
        "braggingRights",
        "emailAddresses",
        "events",
        "genders",
        "imClients",
        "interests",
        "locales",
        "names",
        "nicknames",
        "occupations",
        "organizations",
        "phoneNumbers",
        "relations",
        "residences",
        "skills",
        "urls",
        "userDefined"
    );

    private final GoogleAuthorizationCodeFlow flow;
    private final String syncFlag;

    public PeopleService(GoogleAuthorizationCodeFlow flow, @Value("${syncFlag}") String syncFlag) {
        this.flow = flow;
        this.syncFlag = syncFlag;
    }

    public List<Person> fetchPeople(String userId) {
        LOG.debug("Fetching people for user {}...", userId);

        List<Person> people = new ArrayList<>();

        ListConnectionsResponse response = null;
        String nextPageToken = null;
        do {
            if (response != null) {
                nextPageToken = response.getNextPageToken();
            }

            try {
                response = createClientWithUser(userId)
                    .people()
                    .connections()
                    .list(RESOURCE_NAME)
                    .setPersonFields(StringUtils.join(WRITABLE_PERSON_FIELDS, ","))
                    .setPageToken(nextPageToken)
                    .execute();
            } catch (IOException e) {
                throw new PeopleApiException("Error fetching people: %s", e.getMessage());
            }

            List<Person> connections = response.getConnections();
            if (connections != null) {
                people.addAll(connections);
            }
        } while (response.getNextPageToken() != null);

        people = people.stream()
            .map(this::stripMetadata)
            .filter(this::shouldSync)
            .collect(Collectors.toList());

        LOG.info("Fetched {} {} for user {}", people.size(), people.size() == 1 ? "person" : "people", userId);
        return people;
    }

    public String createPerson(String userId, Person person) {
        LOG.debug("Creating person for user {}...", userId);

        String resourceName;
        try {
            resourceName = createClientWithUser(userId).people()
                .createContact(person)
                .execute()
                .getResourceName();
        } catch (IOException e) {
            throw new PeopleApiException(e, "Error creating person for user {}: %s", userId, e.getMessage());
        }

        LOG.info("Created person {} for user {}", resourceName, userId);
        return resourceName;
    }

    public void updatePerson(String userId, Person person) {
        LOG.debug("Updating person {} for user {}...", person.getResourceName(), userId);

        try {
            createClientWithUser(userId).people()
                .updateContact(person.getResourceName(), person)
                .setUpdatePersonFields(StringUtils.join(WRITABLE_PERSON_FIELDS, ","))
                .execute();
        } catch (IOException e) {
            throw new PeopleApiException(e, "Error updating person {} for user {}: %s", person.getResourceName(), userId, e.getMessage());
        }

        LOG.info("Updated person {} for user {}", person.getResourceName(), userId);
    }

    public void deletePerson(String userId, Person person) {
        LOG.debug("Deleting person {} for user {}...", person.getResourceName(), userId);

        try {
            createClientWithUser(userId).people().deleteContact(person.getResourceName()).execute();
        } catch (IOException e) {
            throw new PeopleApiException(e, "Error deleting user: %s", e.getMessage());
        }

        LOG.info("Deleted person {} for user {}", person.getResourceName(), userId);
    }

    private com.google.api.services.people.v1.PeopleService createClientWithUser(String userId) {
        Credential credential;
        try {
            credential = flow.loadCredential(userId);
        } catch (IOException e) {
            throw new PeopleApiException(e, "Failed to load People API credential: %s", e.getMessage());
        }

        if (credential == null) {
            throw new PeopleApiException("Failed to load People API credential. Credential does not exist for %s", userId);
        }

        return new com.google.api.services.people.v1.PeopleService.Builder(flow.getTransport(), flow.getJsonFactory(), credential)
            .setApplicationName("Contact Sync")
            .build();
    }

    @SuppressWarnings("unchecked")
    private Person stripMetadata(Person person) {
        Person copy = person.clone();

        WRITABLE_PERSON_FIELDS.forEach(fieldName -> {
            List<GenericJson> fieldValues = (List<GenericJson>) copy.get(fieldName);
            if (fieldValues != null) {
                copy.set(fieldName, fieldValues.stream()
                    .filter(item -> ((FieldMetadata) item.get("metadata")).getSource().getType().equals("CONTACT"))
                    .peek(item -> item.set("metadata", null))
                    .collect(Collectors.toList())
                );
            }
        });

        return copy;
    }

    private boolean shouldSync(Person person) {
        List<Biography> biographies = person.getBiographies();
        return biographies != null && biographies.stream().anyMatch(biography -> biography.getValue().contains(syncFlag));
    }
}
