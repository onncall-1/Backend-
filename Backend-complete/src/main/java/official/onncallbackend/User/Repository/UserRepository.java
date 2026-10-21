package official.onncallbackend.User.Repository;

import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Retained for existing auth and phone-based lookup
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndRole(String phone, UserRole role);
}