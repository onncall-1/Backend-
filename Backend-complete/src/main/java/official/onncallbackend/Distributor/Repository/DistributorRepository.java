package official.onncallbackend.Distributor.Repository;

import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Enum.DistributorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistributorRepository
        extends JpaRepository<Distributor, Long> {

    Optional<Distributor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Distributor> findByStatus(DistributorStatus status);
}