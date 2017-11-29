package threewks.contactsync.service.sync;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SyncServiceTest {

    private SyncService syncService;
    private Map<String, SyncItem> statuses;

    @Before
    public void setUp() throws Exception {
        syncService = new SyncService();
        statuses = new LinkedHashMap<>();
    }

    @Test
    public void sync_willCopyAtoB_whenBDoesNotExistAndNotTracked() throws Exception {
        String itemId = "1";
        String a = "a";

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), Collections.emptyMap(), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.CREATE_B, itemId, a, null)));
    }

    @Test
    public void sync_willCopyBtoA_whenADoesNotExistAndNotTracked() throws Exception {
        String itemId = "1";
        String b = "b";

        List<SyncAction<String>> actions = syncService.sync(Collections.emptyMap(), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.CREATE_A, itemId, null, b)));
    }

    @Test
    public void sync_willDeleteA_whenBDoesNotExistAndTracked() throws Exception {
        String itemId = "1";
        String a = "a";

        statuses.put(itemId, SyncItem.create(itemId, a, a));

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), Collections.emptyMap(), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.DELETE_A, itemId, a, null)));
    }

    @Test
    public void sync_willDeleteB_whenADoesNotExistAndTracked() throws Exception {
        String itemId = "1";
        String b = "b";

        statuses.put(itemId, SyncItem.create(itemId, b, b));

        List<SyncAction<String>> actions = syncService.sync(Collections.emptyMap(), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.DELETE_B, itemId, null, b)));
    }

    @Test
    public void sync_willUpdateA_whenBChanged() throws Exception {
        String itemId = "1";
        String a = "a";
        String b = "b";

        statuses.put(itemId, SyncItem.create(itemId, a, a));

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.UPDATE_A, itemId, a, b)));
    }

    @Test
    public void sync_willUpdateB_whenAChanged() throws Exception {
        String itemId = "1";
        String a = "a";
        String b = "b";

        statuses.put(itemId, SyncItem.create(itemId, b, b));

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.UPDATE_B, itemId, a, b)));
    }

    @Test
    public void sync_willGenerateNoAction_whenAandBUnchanged() throws Exception {
        String itemId = "1";
        String a = "a";
        String b = "a";

        statuses.put(itemId, SyncItem.create(itemId, a, b));

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasSize(0));
    }

    @Test
    public void sync_willStopTracking_whenAandBDoNotExist() throws Exception {
        String itemId = "1";
        String a = "a";
        String b = "a";

        statuses.put(itemId, SyncItem.create(itemId, a, b));

        List<SyncAction<String>> actions = syncService.sync(Collections.emptyMap(), Collections.emptyMap(), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.STOP_TRACKING, itemId, null, null)));
    }

    @Test
    public void sync_willGenerateConflict_whenAandBChanged() throws Exception {
        String itemId = "1";
        String a = "a";
        String b = "a";

        statuses.put(itemId, SyncItem.create(itemId, a, b));

        a = "c";
        b = "d";

        List<SyncAction<String>> actions = syncService.sync(ImmutableMap.of(itemId, a), ImmutableMap.of(itemId, b), statuses);

        assertThat(actions, hasItem(new SyncAction<>(SyncAction.Type.CONFLICT, itemId, a, b)));
    }
}
