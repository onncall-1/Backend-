package official.onncallbackend.Provider.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Provider.Enum.VerificationStatus;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final AccessGuard guard;

    public ProviderService(ProviderRepository providerRepository, AccessGuard guard) {
        this.providerRepository = providerRepository;
        this.guard = guard;
    }

    /** Admin only (SecurityConfig). Providers normally register through /api/auth/register. */
    public Provider createProvider(Provider provider) {
        return providerRepository.save(provider);
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public Provider getProviderById(Long id) {
        return find(id);
    }

    /**
     * Providers may edit their own profile and availability status.
     * Verification is NOT editable here; only an admin can change it (see AdminController).
     */
    public Provider updateProvider(Long id, Provider provider) {
        guard.requireProvider(id);
        Provider existing = find(id);

        if (provider.getName() != null && !provider.getName().isBlank()) {
            existing.setName(provider.getName());
        }
        existing.setBio(provider.getBio());
        existing.setProfilePhoto(provider.getProfilePhoto());
        existing.setLatitude(provider.getLatitude());
        existing.setLongitude(provider.getLongitude());
        if (provider.getStatus() != null) {
            existing.setStatus(provider.getStatus());
        }

        return providerRepository.save(existing);
    }

    /** Admin only: approves, rejects, or resets a provider's verification. */
    public Provider setVerificationStatus(Long id, VerificationStatus status) {
        guard.requireAdmin();
        if (status == null) {
            throw ApiException.badRequest("Verification status is required");
        }
        Provider provider = find(id);
        provider.setVerificationStatus(status);
        return providerRepository.save(provider);
    }

    public void deleteProvider(Long id) {
        guard.requireAdmin();
        providerRepository.delete(find(id));
    }

    private Provider find(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Provider not found with id: " + id));
    }
}
