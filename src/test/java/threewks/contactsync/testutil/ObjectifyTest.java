package threewks.contactsync.testutil;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import threewks.contactsync.config.ObjectifyConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ObjectifyConfig.class})
public abstract class ObjectifyTest {
    @Rule
    public SetupAppengine setupAppengine = new SetupAppengine();

    @Rule
    public ObjectifyRollbackRule objectifyRollbackRule = new ObjectifyRollbackRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();
}
