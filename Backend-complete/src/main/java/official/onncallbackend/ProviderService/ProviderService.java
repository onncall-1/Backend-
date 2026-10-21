package official.onncallbackend.ProviderService;

import official.onncallbackend.Provider.Provider;
import official.onncallbackend.ProviderService.Enum.ProviderServiceStatus;
import official.onncallbackend.Service.Service;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "provider_services",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider_id", "service_id"})
        }
)
public class ProviderService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderServiceStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProviderService() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public ProviderServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderServiceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = ProviderServiceStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}