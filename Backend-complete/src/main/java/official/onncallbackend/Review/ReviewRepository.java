
package official.onncallbackend.Review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProviderId(Long providerId);

    List<Review> findByJobId(Long jobId);

    boolean existsByJobIdAndClientId(Long jobId, Long clientId);
}
