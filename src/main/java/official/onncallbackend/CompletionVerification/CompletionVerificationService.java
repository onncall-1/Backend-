
package official.onncallbackend.CompletionVerification;

import official.onncallbackend.Job.JobRepository;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class CompletionVerificationService {

    private final CompletionVerificationRepository verificationRepository;
    private final JobRepository jobRepository;

    private final SecureRandom random = new SecureRandom();

    public CompletionVerificationService(
            CompletionVerificationRepository verificationRepository,
            JobRepository jobRepository) {
        this.verificationRepository = verificationRepository;
        this.jobRepository = jobRepository;
    }

    public CompletionVerification generateOtp(Long jobId) {

        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found");
        }

        CompletionVerification verification =
                new CompletionVerification();

        verification.setJobId(jobId);

        String otp = String.format("%06d", random.nextInt(1_000_000));

        verification.setOtpCode(otp);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verification.setVerified(false);

        return verificationRepository.save(verification);
    }

    public CompletionVerification verifyOtp(Long jobId, String otp) {

        CompletionVerification verification =
                verificationRepository
                        .findTopByJobIdOrderByCreatedAtDesc(jobId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No OTP found for this job"));

        if (verification.isVerified()) {
            throw new IllegalStateException("OTP already verified");
        }

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (!verification.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        verification.setVerified(true);

        return verificationRepository.save(verification);
    }
}
