package esprit.tn.junglepayments.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentIntentResponse {
    private String clientSecret;
    private String publishableKey;
    private String paymentIntentId;
}