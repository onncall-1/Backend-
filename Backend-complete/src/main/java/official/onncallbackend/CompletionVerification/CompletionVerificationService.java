package official.onncallbackend.CompletionVerification;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Proof of completion. The client generates a 6-digit code once the provider arrives,
 * the provider types it in to confirm the work is done, and then the job can be completed.
 */
@Service
public class CompletionVerificationService {

    private static final int MAX_ATTEMPTS = 5;

    private final CompletionVerificationRepository verificationRepository;
    private final JobRepository jobRepository;
    private final AccessGuard guard;
    private final SecureRandom random = new SecureRandom();

    public CompletionVerificationService(
            CompletionVerificationRepository verificationRepository,
            JobRepository jobRepository,
            AccessGuard guard) {
        this.verificationRepository = verificationRepository;
        this.jobRepository = jobRepository;
        this.guard = guard;
    }

    /** Client only. Returns the code so the client can show it to the provider. */
    @Transactional
    public CompletionVerification generateOtp(Long jobId) {
        Job job = findJob(jobId);
        guard.requireClient(job.getClient().getId());

        if (job.getStatus() != JobStatus.ARRIVED && job.getStatus() != JobStatus.IN_PROGRESS) {
            throw ApiException.conflict("A completion code can be generated only after the provider has arrived");
        }

        CompletionVerification verification = new CompletionVerification();
        verification.setJobId(jobId);
        verification.setOtpCode(String.format("%06d", random.nextInt(1_000_000)));
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        verification.setVerified(false);
        verification.setAttempts(0);
        return verificationRepository.save(verification);
    }

    /** Provider only. Confirms the code the client read out. */
    @Transactional(noRollbackFor = ApiException.class)
    public CompletionVerification verifyOtp(Long jobId, String otp) {
        Job job = findJob(jobId);
        guard.requireProvider(job.getProvider().getId());

        CompletionVerification verification = verificationRepository
                .findTopByJobIdOrderByCreatedAtDesc(jobId)
                .orElseThrow(() -> ApiException.badRequest("No completion code has been generated for this job"));

        if (verification.isVerified()) {
            throw ApiException.conflict("This code has already been verified");
        }
        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw ApiException.badRequest("This code has expired. Ask the client to generate a new one.");
        }
        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            throw ApiException.tooManyRequests("Too many wrong attempts. Ask the client to generate a new code.");
        }
        if (otp == null || !otp.equals(verification.getOtpCode())) {
            verification.setAttempts(verification.getAttempts() + 1);
            verificationRepository.save(verification);
            throw ApiException.badRequest("Incorrect code");
        }

        verification.setVerified(true);
        return verificationRepository.save(verification);
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found with id: " + jobId));
    }
}
