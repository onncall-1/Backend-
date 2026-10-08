package official.onncallbackend.ServiceRequest.Service;

import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.Service.Repository.ServiceRepository;
import official.onncallbackend.Service.Service;
import official.onncallbackend.ServiceRequest.Enum.ServiceRequestStatus;
import official.onncallbackend.ServiceRequest.Repository.ServiceRequestRepository;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ClientRepository clientRepository,
            ProviderRepository providerRepository,
            ServiceRepository serviceRepository
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
    }

    public ServiceRequest createRequest(
            Long clientId,
            Long providerId,
            Long serviceId,
            String address,
            Double latitude,
            Double longitude,
            String description
    ) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Client not found with id: " + clientId
                        )
                );

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Provider not found with id: " + providerId
                        )
                );

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service not found with id: " + serviceId
                        )
                );

        if (provider.getVerificationStatus() == null ||
                !provider.getVerificationStatus().name().equals("VERIFIED")) {

            throw new RuntimeException(
                    "Provider is not verified"
            );
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

        return serviceRequestRepository.findAll();
    }

    public ServiceRequest getRequestById(Long id) {

        return serviceRequestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service request not found with id: " + id
                        )
                );
    }

    public List<ServiceRequest> getRequestsByClient(
            Long clientId
    ) {

        return serviceRequestRepository
                .findByClientId(clientId);
    }

    public List<ServiceRequest> getRequestsByProvider(
            Long providerId
    ) {

        return serviceRequestRepository
                .findByProviderId(providerId);
    }

    public List<ServiceRequest> getPendingRequestsForProvider(
            Long providerId
    ) {

        return serviceRequestRepository
                .findByProviderIdAndStatus(
                        providerId,
                        ServiceRequestStatus.PENDING
                );
    }

    public List<ServiceRequest> getRequestsByStatus(
            ServiceRequestStatus status
    ) {

        return serviceRequestRepository
                .findByStatus(status);
    }

    public ServiceRequest updateStatus(
            Long id,
            ServiceRequestStatus status
    ) {

        ServiceRequest request = getRequestById(id);

        request.setStatus(status);

        return serviceRequestRepository.save(request);
    }

    public void deleteRequest(Long id) {

        ServiceRequest request = getRequestById(id);

        serviceRequestRepository.delete(request);
    }
}