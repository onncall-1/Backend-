package official.onncallbackend.User.Repository;

import official.onncallbackend.User.User;
import official.onncallbackend.User.Enum.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndRole(String phone, UserRole role);
}