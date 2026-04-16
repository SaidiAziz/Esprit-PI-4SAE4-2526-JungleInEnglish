package exp.usermicroservice.DTO.Request;

import exp.usermicroservice.Entities.AccountStatus;
import exp.usermicroservice.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    private AccountStatus accountStatus;
}
