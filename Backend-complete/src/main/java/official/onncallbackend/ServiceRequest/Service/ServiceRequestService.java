package official.onncallbackend.ServiceRequest.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Enum.VerificationStatus;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.ProviderService.Repository.ProviderServiceRepository;
import official.onncallbackend.Service.Enum.ServiceStatus;
import official.onncallbackend.Service.Repository.ServiceRepository;
import official.onncallbackend.Service.Service;
import official.onncallbackend.ServiceRequest.Enum.ServiceRequestStatus;
import official.onncallbackend.ServiceRequest.Repository.ServiceRequestRepository;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import official.onncallbackend.common.ApiException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Request lifecycle:
 *   client creates        -> PENDING
 *   provider accepts      -> ACCEPTED   (then POST /api/jobs?serviceRequestId= starts the job)
 *   provider rejects      -> REJECTED
 *   client or provider cancels a PENDING/ACCEPTED request -> CANCELLED
 */
@org.springframework.stereotype.Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final AccessGuard guard;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ClientRepository clientRepository,
            ProviderRepository providerRepository,
            ServiceRepository serviceRepository,
            ProviderServiceRepository providerServiceRepository,
            AccessGuard guard) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
        this.providerServiceRepository = providerServiceRepository;
        this.guard = guard;
    }

    @Transactional
    public ServiceRequest createRequest(
            Long clientId,
            Long providerId,
            Long serviceId,
            String address,
            Double latitude,
            Double longitude,
            String description) {

        guard.requireClient(clientId);

        if (address == null || address.isBlank()) {
            throw ApiException.badRequest("Address is required");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> ApiException.notFound("Client not found with id: " + clientId));
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> ApiException.notFound("Provider not found with id: " + providerId));
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> ApiException.notFound("Service not found with id: " + serviceId));

        if (provider.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw ApiException.forbidden("This provider is not verified yet");
        }
        if (service.getStatus() != ServiceStatus.ACTIVE) {
            throw ApiException.badRequest("This service is not currently available");
        }
        if (!providerServiceRepository.existsByProviderIdAndServiceIdAndStatus(
                providerId, serviceId, ProviderServiceStatus.ACTIVE)) {
            throw ApiException.badRequest("This provider does not offer the selected service");
        }

        ServiceRequest request = new ServiceRequest();
        request.setClient(client);
        request.setProvider(provider);
        request.setService(service);
        request.setAddress(address);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setDescription(description);
        request.setStatus(ServiceRequestStatus.PENDING);

        return serviceRequestRepository.save(request);
    }

    public List<ServiceRequest> getAllRequests() {
        guard.requireAdmin();
        return serviceRequestRepository.findAll();
    }

    public ServiceRequest getRequestById(Long id) {
        ServiceRequest request = find(id);
        guard.requireParticipant(request.getClient().getId(), request.getProvider().getId());
        return request;
    }

    public List<ServiceRequest> getRequestsByClient(Long clientId) {
        guard.requireClient(clientId);
        return serviceRequestRepository.findByClientId(clientId);
    }

    public List<ServiceRequest> getRequestsByProvider(Long providerId) {
        guard.requireProvider(providerId);
        return serviceRequestRepository.findByProviderId(providerId);
    }

    public List<ServiceRequest> getPendingRequestsForProvider(Long providerId) {
        guard.requireProvider(providerId);
        return serviceRequestRepository.findByProviderIdAndStatus(providerId, ServiceRequestStatus.PENDING);
    }

    public List<ServiceRequest> getRequestsByStatus(ServiceRequestStatus status) {
        guard.requireAdmin();
        return serviceRequestRepository.findByStatus(status);
    }

    @Transactional
    public ServiceRequest updateStatus(Long id, ServiceRequestStatus next) {
        ServiceRequest request = getRequestById(id);
        ServiceRequestStatus current = request.getStatus();
        Long clientId = request.getClient().getId();
        Long providerId = request.getProvider().getId();

        switch (next) {
            case ACCEPTED, REJECTED -> {
                if (current != ServiceRequestStatus.PENDING) {
                    throw ApiException.conflict("Only a pending request can be accepted or rejected");
                }
                guard.requireProvider(providerId);
            }
            case CANCELLED -> {
                if (current != ServiceRequestStatus.PENDING && current != ServiceRequestStatus.ACCEPTED) {
                    throw ApiException.conflict("Only a pending or accepted request can be cancelled");
                }
                guard.requireParticipant(clientId, providerId);
            }
            default -> throw ApiException.badRequest("Status " + next + " cannot be set directly");
        }

        request.setStatus(next);
        return serviceRequestRepository.save(request);
    }

    public void deleteRequest(Long id) {
        guard.requireAdmin();
        serviceRequestRepository.delete(find(id));
    }

    private ServiceRequest find(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Service request not found with id: " + id));
    }
}
