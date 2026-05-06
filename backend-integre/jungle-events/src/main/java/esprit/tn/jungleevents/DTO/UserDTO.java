package esprit.tn.jungleevents.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String firstName;  // ✅ correspond à UserResponse
    private String lastName;   // ✅ correspond à UserResponse
    private String email;
    private String role;

}
