
package official.onncallbackend.ProviderAvailability;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProviderAvailabilityRepository
        extends JpaRepository<ProviderAvailability, Long> {

    List<ProviderAvailability> findByProviderId(Long providerId);
}
