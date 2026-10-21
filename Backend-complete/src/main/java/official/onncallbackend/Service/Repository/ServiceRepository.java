package official.onncallbackend.Service.Repository;

import official.onncallbackend.Service.Enum.ServiceStatus;
import official.onncallbackend.Service.Enum.ServiceType;
import official.onncallbackend.Service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByType(ServiceType type);

    List<Service> findByStatus(ServiceStatus status);

    boolean existsByType(ServiceType type);
}