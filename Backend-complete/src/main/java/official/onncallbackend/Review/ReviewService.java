package official.onncallbackend.Review;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Job.Enum.JobStatus;
import official.onncallbackend.Job.Job;
import official.onncallbackend.Job.Repository.JobRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final ProviderRepository providerRepository;
    private final AccessGuard guard;

    public ReviewService(
            ReviewRepository reviewRepository,
            JobRepository jobRepository,
            ProviderRepository providerRepository,
            AccessGuard guard) {
        this.reviewRepository = reviewRepository;
        this.jobRepository = jobRepository;
        this.providerRepository = providerRepository;
        this.guard = guard;
    }

    /**
     * Only the client of a COMPLETED job can review it, once. The provider and client are
     * taken from the job, not from the request body. The provider's average rating is updated.
     */
    @Transactional
    public Review createReview(Review input) {
        if (input.getJobId() == null) {
            throw ApiException.badRequest("jobId is required");
        }
        if (input.getRating() < 1 || input.getRating() > 5) {
            throw ApiException.badRequest("Rating must be between 1 and 5");
        }

        Job job = jobRepository.findById(input.getJobId())
                .orElseThrow(() -> ApiException.notFound("Job not found with id: " + input.getJobId()));

        guard.requireClient(job.getClient().getId());

        if (job.getStatus() != JobStatus.COMPLETED) {
            throw ApiException.conflict("Only completed jobs can be reviewed");
        }
        if (reviewRepository.existsByJobIdAndClientId(job.getId(), job.getClient().getId())) {
            throw ApiException.conflict("You have already reviewed this job");
        }

        Review review = new Review();
        review.setJobId(job.getId());
        review.setClientId(job.getClient().getId());
        review.setProviderId(job.getProvider().getId());
        review.setRating(input.getRating());
        review.setComment(input.getComment());

        Review saved = reviewRepository.save(review);
        refreshProviderRating(job.getProvider().getId());
        return saved;
    }

    public List<Review> getProviderReviews(Long providerId) {
        return reviewRepository.findByProviderId(providerId);
    }

    public List<Review> getJobReviews(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ApiException.notFound("Job not found with id: " + jobId));
        guard.requireParticipant(job.getClient().getId(), job.getProvider().getId());
        return reviewRepository.findByJobId(jobId);
    }

    private void refreshProviderRating(Long providerId) {
        List<Review> reviews = reviewRepository.findByProviderId(providerId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> ApiException.notFound("Provider not found with id: " + providerId));
        provider.setRating(Math.round(average * 10) / 10.0);
        providerRepository.save(provider);
    }
}
