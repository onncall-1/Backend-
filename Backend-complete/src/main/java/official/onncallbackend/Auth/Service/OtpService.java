package official.onncallbackend.Auth.Service;

import official.onncallbackend.Auth.Dto.AuthDtos.OtpRequestResponse;
import official.onncallbackend.Auth.Model.LoginOtp;
import official.onncallbackend.Auth.Repository.LoginOtpRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class OtpService {

    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);

    private final LoginOtpRepository otpRepository;
    private final OtpSender sender;
    private final SecureRandom random = new SecureRandom();
    private final byte[] hmacKey;
    private final long ttlMinutes;
    private final int maxAttempts;
    private final boolean exposeInResponse;

    public OtpService(
            LoginOtpRepository otpRepository,
            OtpSender sender,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.otp.ttl-minutes}") long ttlMinutes,
            @Value("${app.otp.max-attempts}") int maxAttempts,
            @Value("${app.otp.expose-in-response:false}") boolean exposeInResponse) {
        this.otpRepository = otpRepository;
        this.sender = sender;
        this.hmacKey = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlMinutes = ttlMinutes;
        this.maxAttempts = maxAttempts;
        this.exposeInResponse = exposeInResponse;
    }

    /** Creates and sends a new 6-digit code for the phone number. */
    @Transactional
    public OtpRequestResponse issue(String rawPhone) {
        String phone = PhoneNumbers.normalize(rawPhone);

        otpRepository.findTopByPhoneOrderByCreatedAtDesc(phone).ifPresent(last -> {
            if (last.getCreatedAt() != null
                    && Duration.between(last.getCreatedAt(), LocalDateTime.now()).compareTo(RESEND_COOLDOWN) < 0) {
                throw ApiException.tooManyRequests("Please wait 30 seconds before requesting another code");
            }
        });

        String code = String.format("%06d", random.nextInt(1_000_000));

        LoginOtp otp = new LoginOtp();
        otp.setPhone(phone);
        otp.setCodeHash(hash(phone, code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        otp.setAttempts(0);
        otp.setConsumed(false);
        otpRepository.save(otp);

        sender.send(phone, code);

        return new OtpRequestResponse(
                "A verification code has been sent",
                ttlMinutes * 60,
                exposeInResponse ? code : null);
    }

    /**
     * Checks a code and marks it used. Runs in its own transaction and does not roll back on
     * a wrong code, so the failed-attempt counter is saved.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public void verify(String rawPhone, String code) {
        String phone = PhoneNumbers.normalize(rawPhone);

        LoginOtp otp = otpRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> ApiException.badRequest("No code was requested for this phone number"));

        if (otp.isConsumed()) {
            throw ApiException.badRequest("This code has already been used. Request a new one.");
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw ApiException.badRequest("This code has expired. Request a new one.");
        }
        if (otp.getAttempts() >= maxAttempts) {
            throw ApiException.tooManyRequests("Too many wrong attempts. Request a new code.");
        }

        boolean matches = MessageDigest.isEqual(
                hash(phone, code).getBytes(StandardCharsets.UTF_8),
                otp.getCodeHash().getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw ApiException.badRequest("Incorrect code");
        }

        otp.setConsumed(true);
        otpRepository.save(otp);
    }

    private String hash(String phone, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            byte[] out = mac.doFinal((phone + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}
