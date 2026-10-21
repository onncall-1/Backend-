package official.onncallbackend.Admin;

import official.onncallbackend.Provider.Enum.VerificationStatus;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.ProviderDocument.ProviderDocument;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Every endpoint here is admin-only (SecurityConfig: /api/admin/**). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/provider-documents/pending")
    public List<ProviderDocument> getPendingDocuments() {
        return adminService.getPendingDocuments();
    }

    @PatchMapping("/provider-documents/{documentId}/review")
    public ProviderDocument reviewDocument(
            @PathVariable Long documentId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        return adminService.reviewDocument(documentId, approved, rejectionReason);
    }

    /** Approve (VERIFIED), reject (REJECTED), or reset (PENDING) a provider. Clients can only book VERIFIED providers. */
    @PatchMapping("/providers/{providerId}/verification")
    public Provider setProviderVerification(
            @PathVariable Long providerId,
            @RequestParam VerificationStatus status) {
        return adminService.setProviderVerification(providerId, status);
    }
}
