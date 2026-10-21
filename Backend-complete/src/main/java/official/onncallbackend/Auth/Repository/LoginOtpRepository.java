package official.onncallbackend.Auth.Repository;

import official.onncallbackend.Auth.Model.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {

    Optional<LoginOtp> findTopByPhoneOrderByCreatedAtDesc(String phone);
}
