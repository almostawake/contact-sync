package threewks.contactsync.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class GoogleApiAuthService {
    private final GoogleAuthorizationCodeFlow flow;

    public GoogleApiAuthService(GoogleAuthorizationCodeFlow flow) {
        this.flow = flow;
    }

    public String authorise(String callbackUrl) {
        return flow.newAuthorizationUrl()
            .setRedirectUri(callbackUrl)
            .setResponseTypes(Collections.singletonList("code"))
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build();
    }

    public void handleCallback(String callbackUrl, String code) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
            .setRedirectUri(callbackUrl)
            .execute();

        GoogleCredential credential = new GoogleCredential().setAccessToken(tokenResponse.getAccessToken());
        Oauth2 oauth2 = new Oauth2.Builder(flow.getTransport(), flow.getJsonFactory(), credential).build();
        Userinfoplus userInfo = oauth2.userinfo().get().execute();

        flow.createAndStoreCredential(tokenResponse, userInfo.getEmail());
    }

    public Set<String> getStoredCredentialKeys() throws IOException {
        return flow.getCredentialDataStore().keySet();
    }
}
