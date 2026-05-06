package esprit.tn.junglepayments.DTO;

import lombok.Data;

@Data
public class PromoCodeValidationRequest {
    private String promoCode;
    private Long eventId;
    private Double amount;
}
