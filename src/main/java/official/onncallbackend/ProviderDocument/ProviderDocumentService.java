
package official.onncallbackend.ProviderDocument;

import official.onncallbackend.Config.FileStorageService;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProviderDocumentService {

    private final ProviderDocumentRepository documentRepository;
    private final ProviderRepository providerRepository;
    private final FileStorageService fileStorageService;

    public ProviderDocumentService(
            ProviderDocumentRepository documentRepository,
            ProviderRepository providerRepository,
            FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.providerRepository = providerRepository;
        this.fileStorageService = fileStorageService;
    }

    public ProviderDocument upload(
            Long providerId,
            String documentType,
            MultipartFile file) {

        if (!providerRepository.existsById(providerId)) {
            throw new IllegalArgumentException(
                    "Provider not found");
        }

        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException(
                    "Document type is required");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a document");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB");
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
        return documentRepository.findByProviderId(providerId);
    }

    public List<ProviderDocument> getPendingDocuments() {
        return documentRepository.findByStatus(
                ProviderDocumentStatus.PENDING);
    }

    public ProviderDocument review(
            Long documentId,
            boolean approved,
            String rejectionReason) {

        ProviderDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Document not found"));

        if (document.getStatus() !=
                ProviderDocumentStatus.PENDING) {
            throw new IllegalStateException(
                    "This document has already been reviewed");
        }

        if (approved) {
            document.setStatus(ProviderDocumentStatus.APPROVED);
            document.setRejectionReason(null);
        } else {
            if (rejectionReason == null ||
                    rejectionReason.isBlank()) {
                throw new IllegalArgumentException(
                        "Rejection reason is required");
            }

            document.setStatus(ProviderDocumentStatus.REJECTED);
            document.setRejectionReason(rejectionReason);
        }

        document.setReviewedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }
}
