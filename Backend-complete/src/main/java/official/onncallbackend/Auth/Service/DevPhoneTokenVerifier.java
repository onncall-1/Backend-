package official.onncallbackend.Auth.Service;

import official.onncallbackend.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Development only. Accepts tokens like "dev-token:+919876543210" so the frontend can
 * test sign-in without Firebase. Refuses to start under the prod profile.
 */
@Component
@ConditionalOnProperty(name = "app.auth.provider", havingValue = "dev")
public class DevPhoneTokenVerifier implements PhoneTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(DevPhoneTokenVerifier.class);
    private static final String PREFIX = "dev-token:";

    public DevPhoneTokenVerifier(Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("app.auth.provider=dev is not allowed in the prod profile");
        }
        log.warn("DEV AUTH ENABLED: sign-in tokens of the form dev-token:+91... are accepted. Never use in production.");
    }

    @Override
    public String verifyPhone(String idToken) {
        if (idToken == null || !idToken.startsWith(PREFIX)) {
            throw ApiException.unauthorized("Dev token must look like dev-token:+919876543210");
        }
        return PhoneNumbers.normalize(idToken.substring(PREFIX.length()));
    }
}
