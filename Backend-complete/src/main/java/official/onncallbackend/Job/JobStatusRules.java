package official.onncallbackend.Job;

import official.onncallbackend.Job.Enum.JobStatus;

import java.util.Map;
import java.util.Set;

/**
 * The only allowed job status changes. Kept as plain code so it can be unit-tested
 * without starting Spring.
 *
 *   CREATED -> ACCEPTED -> ARRIVED -> IN_PROGRESS -> COMPLETED
 *   any open state (CREATED, ACCEPTED, ARRIVED) -> CANCELLED
 */
public final class JobStatusRules {

    private static final Map<JobStatus, Set<JobStatus>> NEXT = Map.of(
            JobStatus.CREATED, Set.of(JobStatus.ACCEPTED, JobStatus.CANCELLED),
            JobStatus.ACCEPTED, Set.of(JobStatus.ARRIVED, JobStatus.CANCELLED),
            JobStatus.ARRIVED, Set.of(JobStatus.IN_PROGRESS, JobStatus.CANCELLED),
            JobStatus.IN_PROGRESS, Set.of(JobStatus.COMPLETED),
            JobStatus.COMPLETED, Set.<JobStatus>of(),
            JobStatus.CANCELLED, Set.<JobStatus>of()
    );

    private JobStatusRules() {
    }

    public static boolean canTransition(JobStatus from, JobStatus to) {
        return NEXT.getOrDefault(from, Set.of()).contains(to);
    }
}
