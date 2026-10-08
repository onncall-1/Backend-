package official.onncallbackend.ProviderService.Repository;

import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.ProviderService.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderServiceRepository
        extends JpaRepository<ProviderService, Long> {

    boolean existsByProviderIdAndServiceId(
            Long providerId,
            Long serviceId
    );

    List<ProviderService> findByProviderId(Long providerId);

    List<ProviderService> findByServiceId(Long serviceId);

    List<ProviderService> findByProviderIdAndStatus(
            Long providerId,
            ProviderServiceStatus status
    );

    List<ProviderService> findByServiceIdAndStatus(
            Long serviceId,
            ProviderServiceStatus status
    );
}