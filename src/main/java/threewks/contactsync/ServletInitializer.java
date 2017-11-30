package threewks.contactsync;

import contrib.springframework.data.gcp.config.helper.ProfileExtractors;
import contrib.springframework.data.gcp.config.helper.ProfileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.List;

public class ServletInitializer extends SpringBootServletInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ServletInitializer.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        List<String> profiles = new ProfileResolver()
            // Allow a profile if we want to set properties for all gae environments
            .setAdditionalProfileExtractor(ProfileExtractors.staticValue("gae"))
            // Allow us to reference "dev", "uat" without full app id
            .setAdditionalProfileExtractor(ProfileExtractors.AFTER_LAST_DASH)
            .getProfiles();
        LOG.info("Setting profiles: {}", profiles);

        return application.sources(Application.class)
            .profiles(profiles.toArray(new String[profiles.size()]));
    }
}
