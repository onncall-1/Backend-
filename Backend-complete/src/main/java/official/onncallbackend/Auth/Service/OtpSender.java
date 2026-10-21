package official.onncallbackend.Auth.Service;

/**
 * Delivers a one-time code to the user. Implement this with an SMS provider
 * (Twilio, MSG91, etc.) before going live. See LoggingOtpSender for the dev default.
 */
public interface OtpSender {

    void send(String phone, String code);
}
