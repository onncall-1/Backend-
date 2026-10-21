package official.onncallbackend.ProviderAvailability;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderAvailabilityService {

    private final ProviderAvailabilityRepository availabilityRepository;
    private final ProviderRepository providerRepository;
    private final AccessGuard guard;

    public ProviderAvailabilityService(
            ProviderAvailabilityRepository availabilityRepository,
            ProviderRepository providerRepository,
            AccessGuard guard) {
        this.availabilityRepository = availabilityRepository;
        this.providerRepository = providerRepository;
        this.guard = guard;
    }

    public ProviderAvailability create(ProviderAvailability availability) {
        validate(availability);
        guard.requireProvider(availability.getProviderId());
        requireProviderExists(availability.getProviderId());

        availability.setId(null);
        return availabilityRepository.save(availability);
    }

    public List<ProviderAvailability> getByProvider(Long providerId) {
        requireProviderExists(providerId);
        return availabilityRepository.findByProviderId(providerId);
    }

    public ProviderAvailability update(Long id, Long providerId, ProviderAvailability changes) {
        guard.requireProvider(providerId);
        ProviderAvailability existing = find(id, providerId);

        changes.setProviderId(providerId);
        validate(changes);

        existing.setDayOfWeek(changes.getDayOfWeek());
        existing.setStartTime(changes.getStartTime());
        existing.setEndTime(changes.getEndTime());
        existing.setAvailable(changes.isAvailable());
        return availabilityRepository.save(existing);
    }

    public void delete(Long id, Long providerId) {
        guard.requireProvider(providerId);
        availabilityRepository.delete(find(id, providerId));
    }

    private ProviderAvailability find(Long id, Long providerId) {
        ProviderAvailability existing = availabilityRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Availability slot not found"));
        if (!existing.getProviderId().equals(providerId)) {
            throw ApiException.forbidden("Availability slot does not belong to this provider");
        }
        return existing;
    }

    private void requireProviderExists(Long providerId) {
        if (providerId == null || !providerRepository.existsById(providerId)) {
            throw ApiException.notFound("Provider not found");
        }
    }

    private void validate(ProviderAvailability availability) {
        if (availability.getDayOfWeek() == null
                || availability.getStartTime() == null
                || availability.getEndTime() == null) {
            throw ApiException.badRequest("Day, start time, and end time are required");
        }
        if (!availability.getStartTime().isBefore(availability.getEndTime())) {
            throw ApiException.badRequest("Start time must be before end time");
        }
    }
}
