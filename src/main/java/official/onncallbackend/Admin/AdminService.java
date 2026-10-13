
package official.onncallbackend.Admin;

import official.onncallbackend.ProviderDocument.ProviderDocument;
import official.onncallbackend.ProviderDocument.ProviderDocumentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final ProviderDocumentService documentService;

    public AdminService(ProviderDocumentService documentService) {
        this.documentService = documentService;
    }

    public List<ProviderDocument> getPendingDocuments() {
        return documentService.getPendingDocuments();
    }

    public ProviderDocument reviewDocument(
            Long documentId,
            boolean approved,
            String rejectionReason) {

        return documentService.review(
                documentId, approved, rejectionReason);
    }
}
