package official.onncallbackend.ServiceRequest.Controller;

import official.onncallbackend.ServiceRequest.Enum.ServiceRequestStatus;
import official.onncallbackend.ServiceRequest.Service.ServiceRequestService;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(
            ServiceRequestService serviceRequestService
    ) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping
    public ResponseEntity<ServiceRequest> createRequest(

            @RequestParam Long clientId,

            @RequestParam Long providerId,

            @RequestParam Long serviceId,

            @RequestParam String address,

            @RequestParam(required = false) Double latitude,

            @RequestParam(required = false) Double longitude,

            @RequestParam(required = false) String description

    ) {

        return ResponseEntity.ok(
                serviceRequestService.createRequest(
                        clientId,
                        providerId,
                        serviceId,
                        address,
                        latitude,
                        longitude,
                        description
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllRequests() {

        return ResponseEntity.ok(
                serviceRequestService.getAllRequests()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getRequestById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                serviceRequestService.getRequestById(id)
        );
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByClient(
            @PathVariable Long clientId
    ) {

        return ResponseEntity.ok(
                serviceRequestService.getRequestsByClient(clientId)
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByProvider(
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                serviceRequestService.getRequestsByProvider(providerId)
        );
    }

    @GetMapping("/provider/{providerId}/pending")
    public ResponseEntity<List<ServiceRequest>> getPendingRequests(
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                serviceRequestService
                        .getPendingRequestsForProvider(providerId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByStatus(
            @PathVariable ServiceRequestStatus status
    ) {

        return ResponseEntity.ok(
                serviceRequestService
                        .getRequestsByStatus(status)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceRequest> updateStatus(

            @PathVariable Long id,

            @RequestParam ServiceRequestStatus status

    ) {

        return ResponseEntity.ok(
                serviceRequestService.updateStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id
    ) {

        serviceRequestService.deleteRequest(id);

        return ResponseEntity.noContent().build();
    }
}