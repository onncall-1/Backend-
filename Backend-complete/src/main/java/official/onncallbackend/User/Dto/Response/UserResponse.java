package official.onncallbackend.User.Dto.Response;

import official.onncallbackend.User.User;

import java.time.LocalDateTime;

/**
 * Immutable response DTO representing a User.
 * Shields the client from JPA entities, internal relations, and persistence details.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
