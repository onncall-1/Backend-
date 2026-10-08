package official.onncallbackend.ProviderService.Controller;

import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.ProviderService.ProviderService;
import official.onncallbackend.ProviderService.Service.ProviderServiceManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provider-services")
public class ProviderServiceController {

    private final ProviderServiceManager providerServiceManager;

    public ProviderServiceController(
            ProviderServiceManager providerServiceManager
    ) {
        this.providerServiceManager = providerServiceManager;
    }

    @PostMapping
    public ResponseEntity<ProviderService> createProviderService(
            @RequestParam Long providerId,
            @RequestParam Long serviceId
    ) {

        return ResponseEntity.ok(
                providerServiceManager.createProviderService(
                        providerId,
                        serviceId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ProviderService>> getAllProviderServices() {

        return ResponseEntity.ok(
                providerServiceManager.getAllProviderServices()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderService> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                providerServiceManager.getById(id)
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ProviderService>> getByProviderId(
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                providerServiceManager.getByProviderId(providerId)
        );
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ProviderService>> getByServiceId(
            @PathVariable Long serviceId
    ) {

        return ResponseEntity.ok(
                providerServiceManager.getByServiceId(serviceId)
        );
    }

    @GetMapping("/provider/{providerId}/active")
    public ResponseEntity<List<ProviderService>> getActiveByProviderId(
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                providerServiceManager
                        .getActiveByProviderId(providerId)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ProviderService> updateStatus(
            @PathVariable Long id,
            @RequestParam ProviderServiceStatus status
    ) {

        return ResponseEntity.ok(
                providerServiceManager.updateStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProviderService(
            @PathVariable Long id
    ) {

        providerServiceManager.deleteProviderService(id);

        return ResponseEntity.noContent().build();
    }
}