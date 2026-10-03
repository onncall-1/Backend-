package official.onncallbackend.Provider.Service;

import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    // Create Provider
    public Provider createProvider(Provider provider) {
        return providerRepository.save(provider);
    }

    // Get all Providers
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    // Get Provider by ID
    public Provider getProviderById(Long id) {

        return providerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Provider not found with id: " + id));
    }

    // Update Provider
    public Provider updateProvider(Long id, Provider provider) {

        Provider existingProvider = providerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Provider not found with id: " + id));

        existingProvider.setName(provider.getName());
        existingProvider.setBio(provider.getBio());
        existingProvider.setProfilePhoto(provider.getProfilePhoto());
        existingProvider.setLatitude(provider.getLatitude());
        existingProvider.setLongitude(provider.getLongitude());
        existingProvider.setVerificationStatus(provider.getVerificationStatus());
        existingProvider.setStatus(provider.getStatus());

        return providerRepository.save(existingProvider);
    }

    // Delete Provider
    public void deleteProvider(Long id) {

        Provider existingProvider = providerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Provider not found with id: " + id));

        providerRepository.delete(existingProvider);
    }
}