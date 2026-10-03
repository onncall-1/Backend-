package official.onncallbackend.Provider.Controller;

import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    // Create Provider
    @PostMapping
    public ResponseEntity<Provider> createProvider(
            @RequestBody Provider provider) {

        return ResponseEntity.ok(
                providerService.createProvider(provider)
        );
    }

    // Get all Providers
    @GetMapping
    public ResponseEntity<List<Provider>> getAllProviders() {

        return ResponseEntity.ok(
                providerService.getAllProviders()
        );
    }

    // Get Provider by ID
    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                providerService.getProviderById(id)
        );
    }

    // Update Provider
    @PutMapping("/{id}")
    public ResponseEntity<Provider> updateProvider(
            @PathVariable Long id,
            @RequestBody Provider provider) {

        return ResponseEntity.ok(
                providerService.updateProvider(id, provider)
        );
    }

    // Delete Provider
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long id) {

        providerService.deleteProvider(id);

        return ResponseEntity.noContent().build();
    }
}