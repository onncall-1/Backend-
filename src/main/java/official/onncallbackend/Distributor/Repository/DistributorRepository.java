package official.onncallbackend.Distributor.Repository;

import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Enum.DistributorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistributorRepository
        extends JpaRepository<Distributor, Long> {

    Optional<Distributor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Distributor> findByStatus(DistributorStatus status);
}