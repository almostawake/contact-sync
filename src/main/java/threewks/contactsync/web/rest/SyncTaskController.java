package threewks.contactsync.web.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import threewks.contactsync.service.ContactSyncService;

import java.io.IOException;

@RestController
@RequestMapping("/tasks/sync-contacts")
public class SyncTaskController {

    private static final String SYNC_ALL = "all";

    private final ContactSyncService contactSyncService;

    public SyncTaskController(ContactSyncService contactSyncService) {
        this.contactSyncService = contactSyncService;
    }

    @RequestMapping("")
    public String contacts(@RequestParam(value = "userId", defaultValue = SYNC_ALL) String userId) {
        if (userId.equals(SYNC_ALL)) {
            contactSyncService.syncAll();
        } else {
            contactSyncService.syncUser(userId);
        }
        return "Synchronisation complete.";
    }
}
