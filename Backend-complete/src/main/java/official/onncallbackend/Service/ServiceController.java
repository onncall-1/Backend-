package official.onncallbackend.Service;

import official.onncallbackend.Service.Enum.ServiceType;
import official.onncallbackend.Service.Service;
import official.onncallbackend.Service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(
            ServiceService serviceService) {

        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<Service> createService(
            @RequestBody Service service) {

        return ResponseEntity.ok(
                serviceService.createService(service)
        );
    }

    @GetMapping
    public ResponseEntity<List<Service>>
    getAllServices() {

        return ResponseEntity.ok(
                serviceService.getAllServices()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                serviceService.getServiceById(id)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Service> getServiceByType(
            @PathVariable ServiceType type) {

        return ResponseEntity.ok(
                serviceService.getServiceByType(type)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<Service>>
    getActiveServices() {

        return ResponseEntity.ok(
                serviceService.getActiveServices()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(
            @PathVariable Long id,
            @RequestBody Service service) {

        return ResponseEntity.ok(
                serviceService.updateService(
                        id,
                        service
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id) {

        serviceService.deleteService(id);

        return ResponseEntity.noContent().build();
    }
}