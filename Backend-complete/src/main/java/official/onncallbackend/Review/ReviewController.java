
package official.onncallbackend.Review;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @GetMapping("/provider/{providerId}")
    public List<Review> getProviderReviews(
            @PathVariable Long providerId) {
        return reviewService.getProviderReviews(providerId);
    }

    @GetMapping("/job/{jobId}")
    public List<Review> getJobReviews(@PathVariable Long jobId) {
        return reviewService.getJobReviews(jobId);
    }
}
