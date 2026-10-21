package official.onncallbackend.Auth.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import official.onncallbackend.User.Enum.UserRole;

/** Request and response bodies for /api/auth. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record OtpRequest(
            @NotBlank String phone
    ) {
    }

    /** devOtp is only present when the server runs with app.otp.expose-in-response=true (dev/test). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OtpRequestResponse(
            String message,
            long expiresInSeconds,
            String devOtp
    ) {
    }

    public record LoginRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits") String otp
    ) {
    }

    public record RegisterRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits") String otp,
            @NotNull UserRole role,
            String name,
            String businessName,
            String contactName,
            String bio,
            String address,
            Double latitude,
            Double longitude
    ) {
    }

    public record UserSummary(
            Long id,
            String phone,
            UserRole role,
            Long profileId
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserSummary user
    ) {
    }

    public record MeResponse(
            Long id,
            String phone,
            UserRole role,
            Long profileId
    ) {
    }

    public record FirebaseLoginRequest(
            @NotBlank String idToken
    ) {
    }

    public record FirebaseRegisterRequest(
            @NotBlank String idToken,
            @NotNull UserRole role,
            String name,
            String businessName,
            String contactName,
            String bio,
            String address,
            Double latitude,
            Double longitude
    ) {
    }
}
