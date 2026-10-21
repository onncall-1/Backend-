package official.onncallbackend.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import official.onncallbackend.User.Dto.Request.CreateUserRequest;
import official.onncallbackend.User.Dto.Request.UpdateUserRequest;
import official.onncallbackend.User.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Unauthenticated request to /api/users succeeds without JWT")
    void unauthenticatedRequest_Succeeds() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/users creates a user and returns 201 Created without token")
    void createUser_ValidInput_Returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Carol Danvers", "carol@example.com");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Carol Danvers"))
                .andExpect(jsonPath("$.email").value("carol@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/users with invalid input returns 400 Bad Request with field validation errors")
    void createUser_InvalidInput_Returns400() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest("", "not-an-email");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.name").isNotEmpty())
                .andExpect(jsonPath("$.fields.email").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/users with duplicate email returns 409 Conflict")
    void createUser_DuplicateEmail_Returns409() throws Exception {
        CreateUserRequest request1 = new CreateUserRequest("Original User", "duplicate@example.com");
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CreateUserRequest request2 = new CreateUserRequest("Second User", "duplicate@example.com");
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("User already exists with email: duplicate@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 200 OK when user exists")
    void getUserById_ExistingUser_Returns200() throws Exception {
        User user = new User("David Miller", "david@example.com");
        user = userRepository.save(user);

        mvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("David Miller"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 404 Not Found when user does not exist")
    void getUserById_MissingUser_Returns404() throws Exception {
        mvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 999999"));
    }

    @Test
    @DisplayName("GET /api/users returns 200 OK with list of all users")
    void getAllUsers_Returns200() throws Exception {
        userRepository.save(new User("User One", "user1@example.com"));
        userRepository.save(new User("User Two", "user2@example.com"));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("User One"))
                .andExpect(jsonPath("$[1].name").value("User Two"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} updates user and returns 200 OK")
    void updateUser_ValidInput_Returns200() throws Exception {
        User user = userRepository.save(new User("Original Name", "original@example.com"));

        UpdateUserRequest updateRequest = new UpdateUserRequest("Updated Name", "updated@example.com");

        mvc.perform(put("/api/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} with conflicting email returns 409 Conflict")
    void updateUser_ConflictingEmail_Returns409() throws Exception {
        userRepository.save(new User("Existing User", "taken@example.com"));
        User userToUpdate = userRepository.save(new User("Target User", "target@example.com"));

        UpdateUserRequest updateRequest = new UpdateUserRequest("Target User", "taken@example.com");

        mvc.perform(put("/api/users/" + userToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("is already in use by another user")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} deletes user and returns 204 No Content")
    void deleteUser_ExistingUser_Returns204() throws Exception {
        User user = userRepository.save(new User("To Delete", "delete@example.com"));

        mvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} returns 404 Not Found when user does not exist")
    void deleteUser_MissingUser_Returns404() throws Exception {
        mvc.perform(delete("/api/users/888888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
