package official.onncallbackend.Auth.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Default sender: writes the code to the log in dev only. Never logs codes in production. */
@Component
public class LoggingOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpSender.class);

    private final boolean devMode;

    public LoggingOtpSender(@Value("${app.otp.expose-in-response:false}") boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public void send(String phone, String code) {
        if (devMode) {
            log.info("[DEV] OTP for {} is {}", phone, code);
        } else {
            log.warn("No SMS provider configured. OTP for {} was generated but not delivered.", phone);
        }
    }
}
