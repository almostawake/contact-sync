package threewks.contactsync.service.sync;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SyncItemTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void checkStatus_willReturnAChanged_whenAChanges() throws Exception {
        SyncItem syncItem = SyncItem.create("id", "foo", "foo");

        SyncItem.Status status = syncItem.checkStatus("bar", "foo");

        assertThat(status, is(SyncItem.Status.A_CHANGED));
    }

    @Test
    public void checkStatus_willReturnBChanged_whenBChanges() throws Exception {
        SyncItem syncItem = SyncItem.create("id", "foo", "foo");

        SyncItem.Status status = syncItem.checkStatus("foo", "bar");

        assertThat(status, is(SyncItem.Status.B_CHANGED));
    }

    @Test
    public void checkStatus_willReturnBothChanged_whenBothChange() throws Exception {
        SyncItem syncItem = SyncItem.create("id", "foo", "foo");

        SyncItem.Status status = syncItem.checkStatus("bar", "baz");

        assertThat(status, is(SyncItem.Status.BOTH_CHANGED));
    }

    @Test
    public void checkStatus_willReturnNoChange_whenNeitherChange() throws Exception {
        SyncItem syncItem = SyncItem.create("id", "foo", "foo");

        SyncItem.Status status = syncItem.checkStatus("foo", "foo");

        assertThat(status, is(SyncItem.Status.NO_CHANGE));
    }
}
