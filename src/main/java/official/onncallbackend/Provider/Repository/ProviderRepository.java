package official.onncallbackend.Provider.Repository;

import official.onncallbackend.Provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

}