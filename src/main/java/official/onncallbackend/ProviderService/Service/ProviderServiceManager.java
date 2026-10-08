package official.onncallbackend.ProviderService.Service;

import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.ProviderService.ProviderService;
import official.onncallbackend.ProviderService.Repository.ProviderServiceRepository;
import official.onncallbackend.Service.Repository.ServiceRepository;
import official.onncallbackend.Service.Service;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderServiceManager {

    private final ProviderServiceRepository providerServiceRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;

    public ProviderServiceManager(
            ProviderServiceRepository providerServiceRepository,
            ProviderRepository providerRepository,
            ServiceRepository serviceRepository
    ) {
        this.providerServiceRepository = providerServiceRepository;
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
    }

    public ProviderService createProviderService(
            Long providerId,
            Long serviceId
    ) {

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

        if (providerServiceRepository
                .existsByProviderIdAndServiceId(providerId, serviceId)) {

            throw new RuntimeException(
                    "Provider already offers this service"
            );
        }

        ProviderService providerService = new ProviderService();

        providerService.setProvider(provider);
        providerService.setService(service);
        providerService.setStatus(
                ProviderServiceStatus.ACTIVE
        );

        return providerServiceRepository.save(providerService);
    }

    public List<ProviderService> getAllProviderServices() {
        return providerServiceRepository.findAll();
    }

    public ProviderService getById(Long id) {

        return providerServiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Provider service not found with id: " + id
                        )
                );
    }

    public List<ProviderService> getByProviderId(Long providerId) {

        return providerServiceRepository.findByProviderId(providerId);
    }

    public List<ProviderService> getByServiceId(Long serviceId) {

        return providerServiceRepository.findByServiceId(serviceId);
    }

    public List<ProviderService> getActiveByProviderId(
            Long providerId
    ) {

        return providerServiceRepository
                .findByProviderIdAndStatus(
                        providerId,
                        ProviderServiceStatus.ACTIVE
                );
    }

    public ProviderService updateStatus(
            Long id,
            ProviderServiceStatus status
    ) {

        ProviderService providerService = getById(id);

        providerService.setStatus(status);

        return providerServiceRepository.save(providerService);
    }

    public void deleteProviderService(Long id) {

        ProviderService providerService = getById(id);

        providerServiceRepository.delete(providerService);
    }
}