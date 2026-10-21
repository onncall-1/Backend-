package official.onncallbackend.Provider.Repository;

import official.onncallbackend.Provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(Long userId);

    List<Provider> findByVerificationStatus(
            official.onncallbackend.Provider.Enum.VerificationStatus status);
}
