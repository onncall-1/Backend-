package official.onncallbackend.ProviderService.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.ProviderService.ProviderService;
import official.onncallbackend.ProviderService.Repository.ProviderServiceRepository;
import official.onncallbackend.Service.Repository.ServiceRepository;
import official.onncallbackend.Service.Service;
import official.onncallbackend.common.ApiException;

import java.util.List;

@org.springframework.stereotype.Service
public class ProviderServiceManager {

    private final ProviderServiceRepository providerServiceRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final AccessGuard guard;

    public ProviderServiceManager(
            ProviderServiceRepository providerServiceRepository,
            ProviderRepository providerRepository,
            ServiceRepository serviceRepository,
            AccessGuard guard) {
        this.providerServiceRepository = providerServiceRepository;
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
        this.guard = guard;
    }

    public ProviderService createProviderService(Long providerId, Long serviceId) {
        guard.requireProvider(providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> ApiException.notFound("Provider not found with id: " + providerId));
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> ApiException.notFound("Service not found with id: " + serviceId));

        if (providerServiceRepository.existsByProviderIdAndServiceId(providerId, serviceId)) {
            throw ApiException.conflict("Provider already offers this service");
        }

        ProviderService providerService = new ProviderService();
        providerService.setProvider(provider);
        providerService.setService(service);
        providerService.setStatus(ProviderServiceStatus.ACTIVE);
        return providerServiceRepository.save(providerService);
    }

    public List<ProviderService> getAllProviderServices() {
        guard.requireAdmin();
        return providerServiceRepository.findAll();
    }

    public ProviderService getById(Long id) {
        return find(id);
    }

    public List<ProviderService> getByProviderId(Long providerId) {
        return providerServiceRepository.findByProviderId(providerId);
    }

    public List<ProviderService> getByServiceId(Long serviceId) {
        return providerServiceRepository.findByServiceId(serviceId);
    }

    public List<ProviderService> getActiveByProviderId(Long providerId) {
        return providerServiceRepository.findByProviderIdAndStatus(providerId, ProviderServiceStatus.ACTIVE);
    }

    public ProviderService updateStatus(Long id, ProviderServiceStatus status) {
        ProviderService providerService = find(id);
        guard.requireProvider(providerService.getProvider().getId());
        providerService.setStatus(status);
        return providerServiceRepository.save(providerService);
    }

    public void deleteProviderService(Long id) {
        ProviderService providerService = find(id);
        guard.requireProvider(providerService.getProvider().getId());
        providerServiceRepository.delete(providerService);
    }

    private ProviderService find(Long id) {
        return providerServiceRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Provider service not found with id: " + id));
    }
}
