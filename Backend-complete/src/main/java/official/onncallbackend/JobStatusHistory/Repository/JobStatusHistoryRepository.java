package official.onncallbackend.JobStatusHistory.Repository;

import official.onncallbackend.JobStatusHistory.JobStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusHistoryRepository
        extends JpaRepository<JobStatusHistory, Long> {

    List<JobStatusHistory> findByJobIdOrderByCreatedAtAsc(Long jobId);
}