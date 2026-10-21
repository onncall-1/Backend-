package official.onncallbackend.User.Service.impl;

import official.onncallbackend.User.Dto.Request.CreateUserRequest;
import official.onncallbackend.User.Dto.Request.UpdateUserRequest;
import official.onncallbackend.User.Dto.Response.UserResponse;
import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.User.Repository.UserRepository;
import official.onncallbackend.User.Service.UserService;
import official.onncallbackend.User.User;
import official.onncallbackend.exception.DuplicateEmailException;
import official.onncallbackend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Concrete implementation of UserService providing transactional business logic,
 * uniqueness validation, and DTO-Entity mapping.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("User already exists with email: " + normalizedEmail);
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setRole(UserRole.CLIENT);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String normalizedEmail = request.email().trim().toLowerCase();

        // Allow the user to keep their current email, but reject if taken by another user
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException("Email " + normalizedEmail + " is already in use by another user");
        }

        existingUser.setName(request.name().trim());
        existingUser.setEmail(normalizedEmail);

        // Note: id, role, phone, and createdAt are preserved and not overwritten.
        User updatedUser = userRepository.save(existingUser);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(existingUser);
    }
}
