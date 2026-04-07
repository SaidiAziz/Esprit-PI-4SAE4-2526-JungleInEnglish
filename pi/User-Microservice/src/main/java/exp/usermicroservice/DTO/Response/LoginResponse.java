package exp.usermicroservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;           // null if 2FA required
    private UserResponse user;      // null if 2FA required
    private boolean requires2FA;
    private String twoFactorMethod; // "EMAIL" or "TOTP"
    private String email;           // needed for verify-2fa step
}
