package exp.usermicroservice.Services;

import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.DTO.Response.PagedResponse;
import exp.usermicroservice.DTO.Response.UserResponse;
import exp.usermicroservice.Entities.User;

import java.util.List;

public interface UserServiceI {
    User createUser(User user, RegisterUserRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    List<User> getUsers();
  PagedResponse<UserResponse> searchUsers(
    String search, String role, String sortBy, String sortDir, int page, int size
  );
}
