package official.onncallbackend.ServiceRequest.Repository;

import official.onncallbackend.ServiceRequest.Enum.ServiceRequestStatus;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByClientId(Long clientId);

    List<ServiceRequest> findByProviderId(Long providerId);

    List<ServiceRequest> findByServiceId(Long serviceId);

    List<ServiceRequest> findByStatus(
            ServiceRequestStatus status
    );

    List<ServiceRequest> findByProviderIdAndStatus(
            Long providerId,
            ServiceRequestStatus status
    );

    List<ServiceRequest> findByClientIdAndStatus(
            Long clientId,
            ServiceRequestStatus status
    );
}