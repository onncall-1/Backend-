package official.onncallbackend.JobStatusHistory.Repository;

import official.onncallbackend.JobStatusHistory.JobStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobStatusHistoryRepository
        extends JpaRepository<JobStatusHistory, Long> {

    List<JobStatusHistory> findByJobIdOrderByCreatedAtAsc(Long jobId);
}