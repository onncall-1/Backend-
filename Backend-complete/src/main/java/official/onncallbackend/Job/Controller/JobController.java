package official.onncallbackend.Job.Controller;

import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Service.JobService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Job> createJob(
            @RequestParam Long serviceRequestId
    ) {

        return ResponseEntity.ok(
                jobService.createJob(serviceRequestId)
        );
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {

        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                jobService.getJobById(id)
        );
    }

    @GetMapping("/request/{serviceRequestId}")
    public ResponseEntity<Job> getJobByServiceRequest(
            @PathVariable Long serviceRequestId
    ) {

        return ResponseEntity.ok(
                jobService.getJobByServiceRequestId(
                        serviceRequestId
                )
        );
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Job>> getJobsByClient(
            @PathVariable Long clientId
    ) {

        return ResponseEntity.ok(
                jobService.getJobsByClient(clientId)
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Job>> getJobsByProvider(
            @PathVariable Long providerId
    ) {

        return ResponseEntity.ok(
                jobService.getJobsByProvider(providerId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(
            @PathVariable JobStatus status
    ) {

        return ResponseEntity.ok(
                jobService.getJobsByStatus(status)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Job> updateStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status
    ) {

        return ResponseEntity.ok(
                jobService.updateStatus(
                        id,
                        status
                )
        );
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Job> updateFinalPrice(
            @PathVariable Long id,
            @RequestParam Double finalPrice
    ) {

        return ResponseEntity.ok(
                jobService.updateFinalPrice(
                        id,
                        finalPrice
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id
    ) {

        jobService.deleteJob(id);

        return ResponseEntity.noContent().build();
    }
}