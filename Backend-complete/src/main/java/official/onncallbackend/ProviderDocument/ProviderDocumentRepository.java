
package official.onncallbackend.ProviderDocument;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProviderDocumentRepository
        extends JpaRepository<ProviderDocument, Long> {

    List<ProviderDocument> findByProviderId(Long providerId);

    List<ProviderDocument> findByStatus(
            ProviderDocumentStatus status);
}
