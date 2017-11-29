package threewks.contactsync.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.people.v1.PeopleServiceScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Configuration
public class GoogleApiConfig {

    private static final Collection<String> SCOPES = Arrays.asList(
        PeopleServiceScopes.CONTACTS,
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile"
    );

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(HttpTransport httpTransport,
                                                                   JsonFactory jsonFactory,
                                                                   DataStoreFactory dataStoreFactory,
                                                                   @Value("${gmail.api.oauth.clientId}") String clientId,
                                                                   @Value("${gmail.api.oauth.clientSecret}") String clientSecret) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientId, clientSecret, SCOPES)
            .setDataStoreFactory(dataStoreFactory)
            .build();
    }
}
