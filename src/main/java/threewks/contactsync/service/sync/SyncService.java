package threewks.contactsync.service.sync;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Synchroniser service that can synchronise any set of two mutable objects.
 *
 * @see <a href="https://unterwaditzer.net/2016/sync-algorithm.html">https://unterwaditzer.net/2016/sync-algorithm.html</a>
 */
@Service
public class SyncService {

    /**
     * Given two sets of objects keyed by an ID, produces a set of synchronisation actions which can be processed to synchronise the two lists.
     *
     * @param setA the first set
     * @param setB the second set
     * @param statuses a set of status items keyed by ID which tracks the hash value of known items
     * @param <T> the object type being synchronised
     * @return a list of actions
     */
    public <T> List<SyncAction<T>> sync(Map<String, T> setA, Map<String, T> setB, Map<String, SyncItem> statuses) {
        List<SyncAction<T>> actions = new ArrayList<>();

        // Build a combined set of all keys to iterate over
        Set<String> ids = new HashSet<>();
        ids.addAll(setA.keySet());
        ids.addAll(setB.keySet());
        ids.addAll(statuses.keySet());

        for (String id : ids) {
            T a = setA.get(id);
            T b = setB.get(id);
            SyncItem status = statuses.get(id);

            if (a == null && b == null && status != null) {
                actions.add(new SyncAction<>(SyncAction.Type.STOP_TRACKING, id, null, null));
            } else if (a != null && b == null) {
                if (status == null) {
                    actions.add(new SyncAction<>(SyncAction.Type.CREATE_B, id, a, null));
                } else {
                    actions.add(new SyncAction<>(SyncAction.Type.DELETE_A, id, a, null));
                }
            } else if (a == null && b != null) {
                if (status == null) {
                    actions.add(new SyncAction<>(SyncAction.Type.CREATE_A, id, null, b));
                } else {
                    actions.add(new SyncAction<>(SyncAction.Type.DELETE_B, id, null, b));
                }
            } else if (status != null){
                switch (status.checkStatus(a, b)) {
                    case A_CHANGED:
                        actions.add(new SyncAction<>(SyncAction.Type.UPDATE_B, id, a, b));
                        break;
                    case B_CHANGED:
                        actions.add(new SyncAction<>(SyncAction.Type.UPDATE_A, id, a, b));
                        break;
                    case BOTH_CHANGED:
                        actions.add(new SyncAction<>(SyncAction.Type.CONFLICT, id, a, b));
                        break;
                }
            }
        }

        return actions;
    }
}
