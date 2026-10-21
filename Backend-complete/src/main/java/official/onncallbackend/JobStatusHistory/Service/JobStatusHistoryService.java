package official.onncallbackend.JobStatusHistory.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.JobStatusHistory.JobStatusHistory;
import official.onncallbackend.JobStatusHistory.Repository.JobStatusHistoryRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

import java.util.List;

/** The audit trail of job status changes. Entries are written automatically by JobService. */
@Service
public class JobStatusHistoryService {

    private final JobStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;
    private final AccessGuard guard;

    public JobStatusHistoryService(
            JobStatusHistoryRepository historyRepository,
            JobRepository jobRepository,
            AccessGuard guard) {
        this.historyRepository = historyRepository;
        this.jobRepository = jobRepository;
        this.guard = guard;
    }

    /** Admin only (SecurityConfig). Normal history is written by the job workflow. */
    public JobStatusHistory createHistory(Long jobId, JobStatus status, String note) {
        guard.requireAdmin();
        Job job = findJob(jobId);

        JobStatusHistory history = new JobStatusHistory();
        history.setJob(job);
        history.setStatus(status);
        history.setNote(note);
        return historyRepository.save(history);
    }

    public List<JobStatusHistory> getByJobId(Long jobId) {
        Job job = findJob(jobId);
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return historyRepository.findByJobIdOrderByCreatedAtAsc(jobId);
    }

    public JobStatusHistory getById(Long id) {
        JobStatusHistory history = historyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Job status history not found"));
        Job job = history.getJob();
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return history;
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found"));
    }
}
