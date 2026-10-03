package official.onncallbackend.User.Service;

import official.onncallbackend.User.User;
import official.onncallbackend.User.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {


    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    public User createUser(User user) {

        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("User already exists with phone: " + user.getPhone());
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));
    }

    public User updateUser(Long id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));

        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));

        userRepository.delete(existingUser);
    }
}