package official.onncallbackend.Auth.Service;

/** Turns a sign-in token from the frontend into a verified phone number. */
public interface PhoneTokenVerifier {

    /** Returns the verified phone number, or throws ApiException (401) if the token is not valid. */
    String verifyPhone(String idToken);
}
