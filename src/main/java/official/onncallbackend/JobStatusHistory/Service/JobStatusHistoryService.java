package official.onncallbackend.JobStatusHistory.Service;

import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.JobStatusHistory.JobStatusHistory;
import official.onncallbackend.JobStatusHistory.Repository.JobStatusHistoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobStatusHistoryService {

    private final JobStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;

    public JobStatusHistoryService(
            JobStatusHistoryRepository historyRepository,
            JobRepository jobRepository) {

        this.historyRepository = historyRepository;
        this.jobRepository = jobRepository;
    }

    public JobStatusHistory createHistory(
            Long jobId,
            JobStatus status,
            String note) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobStatusHistory history = new JobStatusHistory();

        history.setJob(job);
        history.setStatus(status);
        history.setNote(note);

        return historyRepository.save(history);
    }

    public List<JobStatusHistory> getByJobId(Long jobId) {

        if (!jobRepository.existsById(jobId)) {
            throw new RuntimeException("Job not found");
        }

        return historyRepository.findByJobIdOrderByCreatedAtAsc(jobId);
    }

    public JobStatusHistory getById(Long id) {

        return historyRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Job status history not found"));
    }
}