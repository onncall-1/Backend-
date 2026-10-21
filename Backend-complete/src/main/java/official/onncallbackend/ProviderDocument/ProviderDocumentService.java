package official.onncallbackend.ProviderDocument;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Config.FileStorageService;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProviderDocumentService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final ProviderDocumentRepository documentRepository;
    private final ProviderRepository providerRepository;
    private final FileStorageService fileStorageService;
    private final AccessGuard guard;

    public ProviderDocumentService(
            ProviderDocumentRepository documentRepository,
            ProviderRepository providerRepository,
            FileStorageService fileStorageService,
            AccessGuard guard) {
        this.documentRepository = documentRepository;
        this.providerRepository = providerRepository;
        this.fileStorageService = fileStorageService;
        this.guard = guard;
    }

    public ProviderDocument upload(Long providerId, String documentType, MultipartFile file) {
        guard.requireProvider(providerId);

        if (!providerRepository.existsById(providerId)) {
            throw ApiException.notFound("Provider not found");
        }
        if (documentType == null || documentType.isBlank()) {
            throw ApiException.badRequest("Document type is required");
        }
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Please upload a document");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("File size must not exceed 5 MB");
        }

        String storedName = fileStorageService.store(file);

        ProviderDocument document = new ProviderDocument();
        document.setProviderId(providerId);
        document.setDocumentType(documentType);
        document.setFileName(storedName);
        document.setStatus(ProviderDocumentStatus.PENDING);
        return documentRepository.save(document);
    }

    public List<ProviderDocument> getByProvider(Long providerId) {
        guard.requireProvider(providerId);
        return documentRepository.findByProviderId(providerId);
    }

    /** Admin only (SecurityConfig covers /api/admin/**). */
    public List<ProviderDocument> getPendingDocuments() {
        return documentRepository.findByStatus(ProviderDocumentStatus.PENDING);
    }

    /** Admin only. Approving or rejecting a document is final. */
    public ProviderDocument review(Long documentId, boolean approved, String rejectionReason) {
        ProviderDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found"));

        if (document.getStatus() != ProviderDocumentStatus.PENDING) {
            throw ApiException.conflict("This document has already been reviewed");
        }

        if (approved) {
            document.setStatus(ProviderDocumentStatus.APPROVED);
            document.setRejectionReason(null);
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw ApiException.badRequest("Rejection reason is required");
            }
            document.setStatus(ProviderDocumentStatus.REJECTED);
            document.setRejectionReason(rejectionReason);
        }

        document.setReviewedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }
}
