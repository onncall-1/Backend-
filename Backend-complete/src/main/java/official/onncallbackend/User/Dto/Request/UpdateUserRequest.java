package official.onncallbackend.User.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing user via HTTP PUT (/api/users/{id}).
 * 
 * In standard REST PUT semantics, this endpoint performs a full update
 * of the user's mutable attributes, so both name and email are required.
 */
public record UpdateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email
) {
}
