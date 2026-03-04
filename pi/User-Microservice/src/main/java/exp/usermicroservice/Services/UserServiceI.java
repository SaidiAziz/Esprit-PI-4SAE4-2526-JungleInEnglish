package exp.usermicroservice.Services;

import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.Entities.User;

import java.util.List;

public interface UserServiceI {
    User createUser(User user, RegisterUserRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    List<User> getUsers();
}
