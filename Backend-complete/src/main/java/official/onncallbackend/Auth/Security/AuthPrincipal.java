package official.onncallbackend.Auth.Security;

import official.onncallbackend.User.Enum.UserRole;

/** The identity attached to a request after its JWT has been verified. */
public record AuthPrincipal(Long userId, UserRole role) {
}
