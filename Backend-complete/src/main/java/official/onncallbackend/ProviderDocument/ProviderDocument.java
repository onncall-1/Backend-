
package official.onncallbackend.ProviderDocument;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_documents")
public class ProviderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderDocumentStatus status =
            ProviderDocumentStatus.PENDING;

    private String rejectionReason;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ProviderDocumentStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ProviderDocumentStatus getStatus() { return status; }
    public void setStatus(ProviderDocumentStatus status) {
        this.status = status;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @JsonProperty("url")
    public String getUrl() {
        return fileName == null ? null : "/api/files/" + fileName;
    }

}
