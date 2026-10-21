package official.onncallbackend.Job.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.CompletionVerification.CompletionVerificationRepository;
import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.JobStatusRules;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.JobStatusHistory.JobStatusHistory;
import official.onncallbackend.JobStatusHistory.Repository.JobStatusHistoryRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.ServiceRequest.Enum.ServiceRequestStatus;
import official.onncallbackend.ServiceRequest.Repository.ServiceRequestRepository;
import official.onncallbackend.ServiceRequest.ServiceRequest;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProviderRepository providerRepository;
    private final CompletionVerificationRepository verificationRepository;
    private final JobStatusHistoryRepository historyRepository;
    private final AccessGuard guard;

    public JobService(
            JobRepository jobRepository,
            ServiceRequestRepository serviceRequestRepository,
            ProviderRepository providerRepository,
            CompletionVerificationRepository verificationRepository,
            JobStatusHistoryRepository historyRepository,
            AccessGuard guard) {
        this.jobRepository = jobRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.providerRepository = providerRepository;
        this.verificationRepository = verificationRepository;
        this.historyRepository = historyRepository;
        this.guard = guard;
    }

    /** Starts a job from an ACCEPTED service request. */
    @Transactional
    public Job createJob(Long serviceRequestId) {
        ServiceRequest request = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> ApiException.notFound("Service request not found with id: " + serviceRequestId));

        guard.requireParticipant(request.getClient().getId(), request.getProvider().getId());

        if (request.getStatus() != ServiceRequestStatus.ACCEPTED) {
            throw ApiException.badRequest("Only an accepted service request can start a job");
        }
        if (jobRepository.existsByServiceRequestId(serviceRequestId)) {
            throw ApiException.conflict("A job already exists for service request " + serviceRequestId);
        }

        Job job = new Job();
        job.setServiceRequest(request);
        job.setClient(request.getClient());
        job.setProvider(request.getProvider());
        job.setService(request.getService());
        job.setStatus(JobStatus.CREATED);
        job.setFinalPrice(request.getService().getBasePrice());

        Job saved = jobRepository.save(job);
        recordHistory(saved, JobStatus.CREATED, "Job created from service request " + serviceRequestId);
        return saved;
    }

    public List<Job> getAllJobs() {
        guard.requireAdmin();
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        Job job = find(id);
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return job;
    }

    public Job getJobByServiceRequestId(Long serviceRequestId) {
        Job job = jobRepository.findByServiceRequestId(serviceRequestId)
                .orElseThrow(() -> ApiException.notFound("Job not found for service request: " + serviceRequestId));
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return job;
    }

    public List<Job> getJobsByClient(Long clientId) {
        guard.requireClient(clientId);
        return jobRepository.findByClientId(clientId);
    }

    public List<Job> getJobsByProvider(Long providerId) {
        guard.requireProvider(providerId);
        return jobRepository.findByProviderId(providerId);
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        guard.requireAdmin();
        return jobRepository.findByStatus(status);
    }

    /**
     * Moves a job along its lifecycle. Allowed moves are in JobStatusRules.
     * COMPLETED additionally requires that the client's completion code was verified.
     */
    @Transactional
    public Job updateStatus(Long id, JobStatus next) {
        Job job = getJobById(id);
        JobStatus current = job.getStatus();

        if (!JobStatusRules.canTransition(current, next)) {
            throw ApiException.conflict("A job cannot move from " + current + " to " + next);
        }

        switch (next) {
            case ACCEPTED, ARRIVED, IN_PROGRESS ->
                    guard.requireProvider(job.getProvider().getId());
            case CANCELLED ->
                    guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
            case COMPLETED -> {
                guard.requireProvider(job.getProvider().getId());
                if (!verificationRepository.existsByJobIdAndVerifiedTrue(id)) {
                    throw ApiException.badRequest(
                            "The client's completion code must be verified before the job can be completed");
                }
            }
            default -> throw ApiException.badRequest("Status " + next + " cannot be set directly");
        }

        job.setStatus(next);
        LocalDateTime now = LocalDateTime.now();

        if (next == JobStatus.IN_PROGRESS && job.getStartedAt() == null) {
            job.setStartedAt(now);
        }
        if (next == JobStatus.COMPLETED) {
            job.setCompletedAt(now);
            Provider provider = job.getProvider();
            int done = provider.getCompletedJobs() == null ? 0 : provider.getCompletedJobs();
            provider.setCompletedJobs(done + 1);
            providerRepository.save(provider);
        }

        Job saved = jobRepository.save(job);
        recordHistory(saved, next, null);
        return saved;
    }

    @Transactional
    public Job updateFinalPrice(Long id, Double finalPrice) {
        Job job = getJobById(id);
        guard.requireProvider(job.getProvider().getId());

        if (finalPrice == null || finalPrice < 0) {
            throw ApiException.badRequest("Final price must be zero or more");
        }
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw ApiException.conflict("The price cannot change after a job is completed or cancelled");
        }

        job.setFinalPrice(finalPrice);
        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        guard.requireAdmin();
        jobRepository.delete(find(id));
    }

    private void recordHistory(Job job, JobStatus status, String note) {
        JobStatusHistory entry = new JobStatusHistory();
        entry.setJob(job);
        entry.setStatus(status);
        entry.setNote(note);
        historyRepository.save(entry);
    }

    private Job find(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Job not found with id: " + id));
    }
}
