
package official.onncallbackend.Admin;

import official.onncallbackend.ProviderDocument.ProviderDocument;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/provider-documents")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pending")
    public List<ProviderDocument> getPendingDocuments() {
        return adminService.getPendingDocuments();
    }

    @PatchMapping("/{documentId}/review")
    public ProviderDocument reviewDocument(
            @PathVariable Long documentId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {

        return adminService.reviewDocument(
                documentId, approved, rejectionReason);
    }
}
