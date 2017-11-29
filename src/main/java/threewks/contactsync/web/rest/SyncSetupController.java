package threewks.contactsync.web.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import threewks.contactsync.service.GoogleApiAuthService;

import java.io.IOException;

@RestController
@RequestMapping("/system/sync")
public class SyncSetupController {

    private final GoogleApiAuthService googleApiAuthService;

    private final String callbackUrl;

    public SyncSetupController(@Value("${host}") String host, GoogleApiAuthService googleApiAuthService) {
        this.googleApiAuthService = googleApiAuthService;
        this.callbackUrl = String.format("%s/system/sync/setup/oauth2callback", host);
    }

    @RequestMapping("/setup")
    public RedirectView setup() {
        String url = googleApiAuthService.authorise(callbackUrl);
        return new RedirectView(url);
    }

    @RequestMapping("/setup/oauth2callback")
    public String oauthCallback(@RequestParam("code") String code) throws IOException {
        googleApiAuthService.handleCallback(callbackUrl, code);
        return "Sync setup complete";
    }
}
