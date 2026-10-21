
package official.onncallbackend.Review;

import official.onncallbackend.Job.Repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            JobRepository jobRepository) {
        this.reviewRepository = reviewRepository;
        this.jobRepository = jobRepository;
    }

    public Review createReview(Review review) {

        if (review.getJobId() == null ||
                review.getClientId() == null ||
                review.getProviderId() == null) {
            throw new IllegalArgumentException(
                    "Job, client, and provider IDs are required");
        }

        if (!jobRepository.existsById(review.getJobId())) {
            throw new IllegalArgumentException("Job not found");
        }

        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5");
        }

        if (reviewRepository.existsByJobIdAndClientId(
                review.getJobId(), review.getClientId())) {
            throw new IllegalArgumentException(
                    "Client has already reviewed this job");
        }

        review.setId(null);

        return reviewRepository.save(review);
    }

    public List<Review> getProviderReviews(Long providerId) {
        return reviewRepository.findByProviderId(providerId);
    }

    public List<Review> getJobReviews(Long jobId) {

        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found");
        }

        return reviewRepository.findByJobId(jobId);
    }
}
