package official.onncallbackend.Admin;

import official.onncallbackend.Provider.Enum.VerificationStatus;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Service.ProviderService;
import official.onncallbackend.ProviderDocument.ProviderDocument;
import official.onncallbackend.ProviderDocument.ProviderDocumentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final ProviderDocumentService documentService;
    private final ProviderService providerService;

    public AdminService(ProviderDocumentService documentService, ProviderService providerService) {
        this.documentService = documentService;
        this.providerService = providerService;
    }

    public List<ProviderDocument> getPendingDocuments() {
        return documentService.getPendingDocuments();
    }

    public ProviderDocument reviewDocument(Long documentId, boolean approved, String rejectionReason) {
        return documentService.review(documentId, approved, rejectionReason);
    }

    public Provider setProviderVerification(Long providerId, VerificationStatus status) {
        return providerService.setVerificationStatus(providerId, status);
    }
}
