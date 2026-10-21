package official.onncallbackend.User;

import official.onncallbackend.User.Dto.Request.CreateUserRequest;
import official.onncallbackend.User.Dto.Request.UpdateUserRequest;
import official.onncallbackend.User.Dto.Response.UserResponse;
import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.User.Repository.UserRepository;
import official.onncallbackend.User.Service.impl.UserServiceImpl;
import official.onncallbackend.exception.DuplicateEmailException;
import official.onncallbackend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("Alice Johnson");
        sampleUser.setEmail("alice@example.com");
        sampleUser.setRole(UserRole.CLIENT);
        sampleUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        sampleUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("createUser should save and return UserResponse when email is unique")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest("Alice Johnson", "alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Johnson");
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser should throw DuplicateEmailException when email is already taken")
    void createUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        CreateUserRequest request = new CreateUserRequest("Alice Johnson", "alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("User already exists with email: alice@example.com");

        verify(userRepository).existsByEmail("alice@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById should return UserResponse when user exists")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Johnson");
        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("getUserById should throw ResourceNotFoundException when user does not exist")
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("getAllUsers should return all users mapped to UserResponse list")
    void getAllUsers_Success() {
        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Bob Smith");
        secondUser.setEmail("bob@example.com");
        secondUser.setRole(UserRole.CLIENT);
        secondUser.setCreatedAt(LocalDateTime.now());
        secondUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(sampleUser, secondUser));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Alice Johnson");
        assertThat(responses.get(1).name()).isEqualTo("Bob Smith");
    }

    @Test
    @DisplayName("updateUser should update and return UserResponse when email is valid and available")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest("Alice Updated", "alice.updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailAndIdNot("alice.updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("alice.updated@example.com", 1L);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateUser should allow user to retain their existing email without throwing exception")
    void updateUser_SameEmail_Success() {
        UpdateUserRequest request = new UpdateUserRequest("Alice Updated", "alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailAndIdNot("alice@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateUser should throw DuplicateEmailException when email belongs to another user")
    void updateUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        UpdateUserRequest request = new UpdateUserRequest("Alice Johnson", "taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Email taken@example.com is already in use by another user");

        verify(userRepository, never()).save(sampleUser);
    }

    @Test
    @DisplayName("updateUser should throw ResourceNotFoundException when user does not exist")
    void updateUser_NotFound_ThrowsResourceNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest("Ghost", "ghost@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("deleteUser should delete user when user exists")
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(sampleUser);
    }

    @Test
    @DisplayName("deleteUser should throw ResourceNotFoundException when user does not exist")
    void deleteUser_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository, never()).delete(any());
    }
}
