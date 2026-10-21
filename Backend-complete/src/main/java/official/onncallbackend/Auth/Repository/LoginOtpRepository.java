package official.onncallbackend.Auth.Repository;

import official.onncallbackend.Auth.Model.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {

    Optional<LoginOtp> findTopByPhoneOrderByCreatedAtDesc(String phone);
}
