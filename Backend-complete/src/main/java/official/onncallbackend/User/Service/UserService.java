package official.onncallbackend.User.Service;

import official.onncallbackend.User.Dto.Request.CreateUserRequest;
import official.onncallbackend.User.Dto.Request.UpdateUserRequest;
import official.onncallbackend.User.Dto.Response.UserResponse;

import java.util.List;

/**
 * Service interface declaring core business operations for User CRUD.
 * Acts as the boundary between the web/controller layer and the persistence layer.
 */
public interface UserService {

    /**
     * Creates a new user after verifying that the email is not already registered.
     *
     * @param request validated user creation payload
     * @return created user representation
     * @throws official.onncallbackend.exception.DuplicateEmailException if the email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Retrieves a user by their unique database identifier.
     *
     * @param id primary key identifier
     * @return user representation
     * @throws official.onncallbackend.exception.ResourceNotFoundException if no user exists with this ID
     */
    UserResponse getUserById(Long id);

    /**
     * Retrieves all registered users.
     *
     * @return list of all users
     */
    List<UserResponse> getAllUsers();

    /**
     * Updates an existing user's details (name and email).
     *
     * @param id primary key identifier
     * @param request validated user update payload
     * @return updated user representation
     * @throws official.onncallbackend.exception.ResourceNotFoundException if user does not exist
     * @throws official.onncallbackend.exception.DuplicateEmailException if the email is taken by another user
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Deletes a user by their unique database identifier.
     *
     * @param id primary key identifier
     * @throws official.onncallbackend.exception.ResourceNotFoundException if user does not exist
     */
    void deleteUser(Long id);
}