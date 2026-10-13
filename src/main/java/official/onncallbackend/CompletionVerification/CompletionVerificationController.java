
package official.onncallbackend.CompletionVerification;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/completion-verification")

public class CompletionVerificationController {

    private final CompletionVerificationService verificationService;

    public CompletionVerificationController(
            CompletionVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/generate/{jobId}")
    public CompletionVerification generateOtp(
            @PathVariable Long jobId) {
        return verificationService.generateOtp(jobId);
    }

    @PostMapping("/verify/{jobId}")
    public CompletionVerification verifyOtp(
            @PathVariable Long jobId,
            @RequestParam String otp) {
        return verificationService.verifyOtp(jobId, otp);
    }
}
