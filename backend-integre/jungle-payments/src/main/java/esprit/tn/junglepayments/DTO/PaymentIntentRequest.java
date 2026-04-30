// DTO/PaymentIntentRequest.java
package esprit.tn.junglepayments.DTO;

import lombok.Data;

@Data
public class PaymentIntentRequest {
    private Double  amount;
    private String  eventTitle;
    private Long    eventId;
    private String  userEmail;
    private String  promoCode;
}