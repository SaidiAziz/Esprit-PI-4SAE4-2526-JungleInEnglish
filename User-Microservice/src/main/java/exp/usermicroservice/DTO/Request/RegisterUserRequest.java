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
public class RegisterUserRequest {
    // User data (required for signup)
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // Optional: student profile data (used when role = STUDENT)
    private String level;
    private String learningGoals;

    // Optional: tutor profile data (used when role = TUTOR)
    private String bio;
    private String specialization;
    private Integer experienceYears;
    private Float hourlyRate;
}
