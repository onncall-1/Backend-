package official.onncallbackend.Job;

import official.onncallbackend.Job.Enum.JobStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobStatusRulesTest {

    @Test
    void happyPathIsAllowedStepByStep() {
        assertTrue(JobStatusRules.canTransition(JobStatus.CREATED, JobStatus.ACCEPTED));
        assertTrue(JobStatusRules.canTransition(JobStatus.ACCEPTED, JobStatus.ARRIVED));
        assertTrue(JobStatusRules.canTransition(JobStatus.ARRIVED, JobStatus.IN_PROGRESS));
        assertTrue(JobStatusRules.canTransition(JobStatus.IN_PROGRESS, JobStatus.COMPLETED));
    }

    @Test
    void stepsCannotBeSkipped() {
        assertFalse(JobStatusRules.canTransition(JobStatus.ACCEPTED, JobStatus.COMPLETED));
        assertFalse(JobStatusRules.canTransition(JobStatus.CREATED, JobStatus.IN_PROGRESS));
    }

    @Test
    void openJobsCanBeCancelled() {
        assertTrue(JobStatusRules.canTransition(JobStatus.CREATED, JobStatus.CANCELLED));
        assertTrue(JobStatusRules.canTransition(JobStatus.ACCEPTED, JobStatus.CANCELLED));
        assertTrue(JobStatusRules.canTransition(JobStatus.ARRIVED, JobStatus.CANCELLED));
    }

    @Test
    void completedAndCancelledAreFinal() {
        for (JobStatus to : JobStatus.values()) {
            assertFalse(JobStatusRules.canTransition(JobStatus.COMPLETED, to));
            assertFalse(JobStatusRules.canTransition(JobStatus.CANCELLED, to));
        }
    }
}
