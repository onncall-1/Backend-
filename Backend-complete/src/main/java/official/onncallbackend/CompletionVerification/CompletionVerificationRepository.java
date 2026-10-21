
package official.onncallbackend.CompletionVerification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompletionVerificationRepository
        extends JpaRepository<CompletionVerification, Long> {

    Optional<CompletionVerification>
    findTopByJobIdOrderByCreatedAtDesc(Long jobId);

    boolean existsByJobIdAndVerifiedTrue(Long jobId);
}
