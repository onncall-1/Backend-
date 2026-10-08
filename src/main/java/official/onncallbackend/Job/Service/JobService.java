package official.onncallbackend.Job.Service;

import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import official.onncallbackend.ServiceRequest.Repository.ServiceRequestRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public JobService(
            JobRepository jobRepository,
            ServiceRequestRepository serviceRequestRepository
    ) {
        this.jobRepository = jobRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public Job createJob(Long serviceRequestId) {

        ServiceRequest request =
                serviceRequestRepository.findById(serviceRequestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service request not found with id: "
                                                + serviceRequestId
                                )
                        );

        if (jobRepository.existsByServiceRequestId(serviceRequestId)) {

            throw new RuntimeException(
                    "Job already exists for service request: "
                            + serviceRequestId
            );
        }

        Job job = new Job();

        job.setServiceRequest(request);
        job.setClient(request.getClient());
        job.setProvider(request.getProvider());
        job.setService(request.getService());

        job.setStatus(JobStatus.CREATED);

        job.setFinalPrice(
                request.getService().getBasePrice()
        );

        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {

        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        )
                );
    }

    public Job getJobByServiceRequestId(
            Long serviceRequestId
    ) {

        return jobRepository
                .findByServiceRequestId(serviceRequestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found for service request: "
                                        + serviceRequestId
                        )
                );
    }

    public List<Job> getJobsByClient(Long clientId) {

        return jobRepository.findByClientId(clientId);
    }

    public List<Job> getJobsByProvider(Long providerId) {

        return jobRepository.findByProviderId(providerId);
    }

    public List<Job> getJobsByStatus(JobStatus status) {

        return jobRepository.findByStatus(status);
    }

    public Job updateStatus(
            Long id,
            JobStatus status
    ) {

        Job job = getJobById(id);

        job.setStatus(status);

        if (status == JobStatus.IN_PROGRESS) {

            if (job.getStartedAt() == null) {
                job.setStartedAt(LocalDateTime.now());
            }
        }

        if (status == JobStatus.COMPLETED) {

            job.setCompletedAt(LocalDateTime.now());
        }

        return jobRepository.save(job);
    }

    public Job updateFinalPrice(
            Long id,
            Double finalPrice
    ) {

        Job job = getJobById(id);

        job.setFinalPrice(finalPrice);

        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {

        Job job = getJobById(id);

        jobRepository.delete(job);
    }
}