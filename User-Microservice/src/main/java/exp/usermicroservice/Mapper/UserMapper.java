package exp.usermicroservice.Mapper;

import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.DTO.Request.UpdateUserRequest;
import exp.usermicroservice.DTO.Response.UserResponse;
import exp.usermicroservice.Entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterUserRequest request) {
        if (request == null) return null;
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setAccountStatus(request.getAccountStatus() != null ? request.getAccountStatus() : exp.usermicroservice.Entities.AccountStatus.ACTIVE);
        return user;
    }

    public void updateEntity(User existing, UpdateUserRequest request) {
        if (existing == null || request == null) return;
        if (request.getFirstName() != null) existing.setFirstName(request.getFirstName());
        if (request.getLastName() != null) existing.setLastName(request.getLastName());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getPassword() != null) existing.setPassword(request.getPassword());
        if (request.getRole() != null) existing.setRole(request.getRole());
        if (request.getAccountStatus() != null) existing.setAccountStatus(request.getAccountStatus());
    }

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
