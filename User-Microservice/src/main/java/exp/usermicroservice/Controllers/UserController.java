package exp.usermicroservice.Controllers;

import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.DTO.Request.UpdateUserRequest;
import exp.usermicroservice.DTO.Response.UserResponse;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Mapper.UserMapper;
import exp.usermicroservice.Security.SecurityUtils;
import exp.usermicroservice.Services.UserServiceI;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserServiceI userService;
    private final UserMapper userMapper;

    @PostMapping("/addUser")
    public ResponseEntity<UserResponse> createUser(@RequestBody RegisterUserRequest request) {
        User user = userMapper.toEntity(request);
        User created = userService.createUser(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(created));
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UpdateUserRequest request) {
        User existing = userService.getUserById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        userMapper.updateEntity(existing, request);
        User updated = userService.updateUser(id, existing);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        if (userService.getUserById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/getAllUsers")
    public List<UserResponse> getAllUsers() {
        return userService.getUsers().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
