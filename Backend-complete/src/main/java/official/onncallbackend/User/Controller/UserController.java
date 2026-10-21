package official.onncallbackend.User.Controller;

import jakarta.validation.Valid;
import official.onncallbackend.User.Dto.Request.CreateUserRequest;
import official.onncallbackend.User.Dto.Request.UpdateUserRequest;
import official.onncallbackend.User.Dto.Response.UserResponse;
import official.onncallbackend.User.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for User CRUD operations.
 * Maps incoming HTTP requests to UserService operations and formats HTTP responses.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users
     * Creates a new user.
     *
     * @param request validated CreateUserRequest payload
     * @return 201 Created with UserResponse
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/users/{id}
     * Retrieves a user by their unique identifier.
     *
     * @param id user primary key
     * @return 200 OK with UserResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users
     * Lists all registered users.
     *
     * @return 200 OK with list of UserResponse
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/users/{id}
     * Updates an existing user's details.
     *
     * @param id user primary key
     * @param request validated UpdateUserRequest payload
     * @return 200 OK with updated UserResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/users/{id}
     * Deletes a user by their unique identifier.
     *
     * @param id user primary key
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}