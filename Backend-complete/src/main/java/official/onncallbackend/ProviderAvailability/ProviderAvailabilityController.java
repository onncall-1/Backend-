
package official.onncallbackend.ProviderAvailability;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/provider-availability")
public class ProviderAvailabilityController {

    private final ProviderAvailabilityService availabilityService;

    public ProviderAvailabilityController(
            ProviderAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ProviderAvailability create(
            @RequestBody ProviderAvailability availability) {
        return availabilityService.create(availability);
    }

    @GetMapping("/provider/{providerId}")
    public List<ProviderAvailability> getByProvider(
            @PathVariable Long providerId) {
        return availabilityService.getByProvider(providerId);
    }

    @PutMapping("/{id}/provider/{providerId}")
    public ProviderAvailability update(
            @PathVariable Long id,
            @PathVariable Long providerId,
            @RequestBody ProviderAvailability availability) {
        return availabilityService.update(
                id, providerId, availability);
    }

    @DeleteMapping("/{id}/provider/{providerId}")
    public void delete(
            @PathVariable Long id,
            @PathVariable Long providerId) {
        availabilityService.delete(id, providerId);
    }
}
