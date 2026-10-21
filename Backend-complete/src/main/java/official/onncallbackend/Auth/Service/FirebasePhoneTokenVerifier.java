package official.onncallbackend.Auth.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import official.onncallbackend.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Verifies Firebase phone sign-in ID tokens.
 * Needs the GOOGLE_APPLICATION_CREDENTIALS environment variable to point at the
 * Firebase service-account JSON file.
 */
@Component
@ConditionalOnProperty(name = "app.auth.provider", havingValue = "firebase")
public class FirebasePhoneTokenVerifier implements PhoneTokenVerifier {

    public FirebasePhoneTokenVerifier() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseApp.initializeApp();
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "Firebase credentials not found. Set GOOGLE_APPLICATION_CREDENTIALS to the "
                                + "path of your Firebase service-account JSON file.", e);
            }
        }
    }

    @Override
    public String verifyPhone(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw ApiException.unauthorized("Firebase sign-in token is required");
        }
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            Object phone = token.getClaims().get("phone_number");
            if (!(phone instanceof String number) || number.isBlank()) {
                throw ApiException.unauthorized("This sign-in has no verified phone number");
            }
            return PhoneNumbers.normalize(number);
        } catch (FirebaseAuthException e) {
            throw ApiException.unauthorized("Sign-in expired or invalid. Please sign in again.");
        }
    }
}
