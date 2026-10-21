package official.onncallbackend.Client.Repository;

import official.onncallbackend.Client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

}