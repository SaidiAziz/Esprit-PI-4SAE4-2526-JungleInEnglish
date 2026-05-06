package exp.usermicroservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotpSetupResponse {
    private String secret;
    private String qrCodeBase64;
}

