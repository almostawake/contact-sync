package threewks.contactsync.testutil;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import org.apache.commons.text.RandomStringGenerator;

import java.util.Arrays;

import static threewks.contactsync.testutil.RandomNameGenerator.randomFirstName;
import static threewks.contactsync.testutil.RandomNameGenerator.randomLastName;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class TestData {

    public static final RandomStringGenerator RANDOM_STRING_GENERATOR = new RandomStringGenerator.Builder().withinRange('0', '9').build();

    public static String generatePersonResourceName() {
        return String.format("people/c%s", RANDOM_STRING_GENERATOR.generate(19));
    }

    public static Person generatePerson() {
        String resourceName = generatePersonResourceName();
        Name name = generateName();

        return new Person()
            .setResourceName(resourceName)
            .setNames(Arrays.asList(name))
            .setEmailAddresses(Arrays.asList(generateEmailAddress(name)));
    }

    private static Name generateName() {
        return new Name()
            .setGivenName(randomFirstName())
            .setFamilyName(randomLastName());
    }

    private static EmailAddress generateEmailAddress(Name name) {
        String email = String.format("%s.%s@example.org", name.getGivenName(), name.getFamilyName());
        return new EmailAddress().setValue(email);
    }
}
