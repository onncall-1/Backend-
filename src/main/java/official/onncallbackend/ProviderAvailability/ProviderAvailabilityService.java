
package official.onncallbackend.ProviderAvailability;

import official.onncallbackend.Provider.Repository.ProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderAvailabilityService {

    private final ProviderAvailabilityRepository availabilityRepository;
    private final ProviderRepository providerRepository;

    public ProviderAvailabilityService(
            ProviderAvailabilityRepository availabilityRepository,
            ProviderRepository providerRepository) {
        this.availabilityRepository = availabilityRepository;
        this.providerRepository = providerRepository;
    }

    public ProviderAvailability create(
            ProviderAvailability availability) {

        validate(availability);

        if (!providerRepository.existsById(
                availability.getProviderId())) {
            throw new IllegalArgumentException(
                    "Provider not found");
        }

        availability.setId(null);
        return availabilityRepository.save(availability);
    }

    public List<ProviderAvailability> getByProvider(
            Long providerId) {

        if (!providerRepository.existsById(providerId)) {
            throw new IllegalArgumentException(
                    "Provider not found");
        }

        return availabilityRepository.findByProviderId(providerId);
    }

    public ProviderAvailability update(
            Long id,
            Long providerId,
            ProviderAvailability changes) {

        ProviderAvailability existing =
                availabilityRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Availability slot not found"));

        if (!existing.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException(
                    "Availability slot does not belong to this provider");
        }

        changes.setProviderId(providerId);
        validate(changes);

        existing.setDayOfWeek(changes.getDayOfWeek());
        existing.setStartTime(changes.getStartTime());
        existing.setEndTime(changes.getEndTime());
        existing.setAvailable(changes.isAvailable());

        return availabilityRepository.save(existing);
    }

    public void delete(Long id, Long providerId) {
        ProviderAvailability existing =
                availabilityRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Availability slot not found"));

        if (!existing.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException(
                    "Availability slot does not belong to this provider");
        }

        availabilityRepository.delete(existing);
    }

    private void validate(ProviderAvailability availability) {
        if (availability.getDayOfWeek() == null ||
                availability.getStartTime() == null ||
                availability.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Day, start time, and end time are required");
        }

        if (!availability.getStartTime()
                .isBefore(availability.getEndTime())) {
            throw new IllegalArgumentException(
                    "Start time must be before end time");
        }
    }
}
