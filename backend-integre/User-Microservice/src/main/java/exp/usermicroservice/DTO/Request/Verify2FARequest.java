package exp.usermicroservice.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Verify2FARequest {
    private String email;
    private String code;
}

