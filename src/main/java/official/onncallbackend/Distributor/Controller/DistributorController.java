package official.onncallbackend.Distributor.Controller;

import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Service.DistributorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distributors")
public class DistributorController {

    private final DistributorService distributorService;

    public DistributorController(
            DistributorService distributorService) {

        this.distributorService = distributorService;
    }

    @PostMapping
    public ResponseEntity<Distributor> createDistributor(
            @RequestBody Distributor distributor) {

        return ResponseEntity.ok(
                distributorService.createDistributor(distributor)
        );
    }

    @GetMapping
    public ResponseEntity<List<Distributor>> getAllDistributors() {

        return ResponseEntity.ok(
                distributorService.getAllDistributors()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distributor> getDistributorById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                distributorService.getDistributorById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Distributor> getDistributorByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                distributorService.getDistributorByUserId(userId)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<Distributor>> getActiveDistributors() {

        return ResponseEntity.ok(
                distributorService.getActiveDistributors()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Distributor> updateDistributor(
            @PathVariable Long id,
            @RequestBody Distributor distributor) {

        return ResponseEntity.ok(
                distributorService.updateDistributor(
                        id,
                        distributor
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDistributor(
            @PathVariable Long id) {

        distributorService.deleteDistributor(id);

        return ResponseEntity.noContent().build();
    }
}