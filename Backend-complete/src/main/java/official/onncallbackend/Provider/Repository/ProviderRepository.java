package official.onncallbackend.Provider.Repository;

import official.onncallbackend.Provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(Long userId);

    List<Provider> findByVerificationStatus(
            official.onncallbackend.Provider.Enum.VerificationStatus status);
}
