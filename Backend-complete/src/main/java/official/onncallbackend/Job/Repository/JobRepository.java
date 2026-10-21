package official.onncallbackend.Job.Repository;

import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByServiceRequestId(Long serviceRequestId);

    List<Job> findByClientId(Long clientId);

    List<Job> findByProviderId(Long providerId);

    List<Job> findByServiceId(Long serviceId);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByProviderIdAndStatus(
            Long providerId,
            JobStatus status
    );

    boolean existsByServiceRequestId(Long serviceRequestId);
}