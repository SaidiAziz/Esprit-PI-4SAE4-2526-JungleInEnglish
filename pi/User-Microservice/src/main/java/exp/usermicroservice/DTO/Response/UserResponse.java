package exp.usermicroservice.DTO.Response;

import exp.usermicroservice.Entities.AccountStatus;
import exp.usermicroservice.Entities.Role;
import exp.usermicroservice.Entities.TwoFactorMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private Role role;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean twoFactorEnabled;
    private TwoFactorMethod twoFactorMethod;
}
