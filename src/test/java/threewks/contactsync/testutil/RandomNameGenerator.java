package threewks.contactsync.testutil;

import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.List;

public class RandomNameGenerator {

    private static final List<String> FIRST_NAMES = Arrays.asList(
        "Maliyah", "Aydin", "Kian", "Helena", "Micah", "Rihanna", "Conor", "Breanna", "Abril", "Deangelo", "Marianna", "Maximillian", "Shayna", "Alexis", "Alex", "Angelina", "Michelle", "Shyla", "Timothy", "Maryjane", "Mohamed", "Joaquin", "Nadia", "Karley", "Angelica", "Ellis", "Steven", "Isai", "April", "Harley", "Giancarlo", "Luna", "Francesca", "Madelyn", "Esperanza", "Makaila", "Liberty", "Aylin", "Alaina", "Dalton", "Teagan", "Whitney", "Annalise", "Abram", "Gunner", "Randall", "Gracelyn", "Davis", "Nigel", "Javier"
    );
    private static final List<String> LAST_NAMES = Arrays.asList(
        "Harris", "Erickson", "Bonilla", "Rivers", "Frey", "Hutchinson", "Moon", "Palmer", "Pierce", "Williamson", "Winters", "Lawson", "Daugherty", "Skinner", "Rubio", "Grant", "Mcclain", "Brady", "Patel", "Huber", "Levine", "Mcdaniel", "Forbes", "Coffey", "Mayo", "Wu", "Nunez", "Summers", "Houston", "Singh", "Townsend", "Stafford", "Hoffman", "Hensley", "Huang", "Cervantes", "Gibson", "Bell", "Harvey", "Shaw", "Allison", "Newman", "Mclaughlin", "Patton", "Bond", "Medina", "Ochoa", "Gray", "Whitehead", "Burgess"
    );

    public static String randomFirstName() {
        return FIRST_NAMES.get(RandomUtils.nextInt(0, FIRST_NAMES.size()));
    }

    public static String randomLastName() {
        return LAST_NAMES.get(RandomUtils.nextInt(0, LAST_NAMES.size()));
    }

    public static String randomName() {
        return String.format("%s %s", randomFirstName(), randomLastName());
    }
}
