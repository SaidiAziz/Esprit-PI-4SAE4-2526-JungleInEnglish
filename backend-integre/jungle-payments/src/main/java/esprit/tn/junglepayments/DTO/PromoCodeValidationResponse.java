package esprit.tn.junglepayments.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromoCodeValidationResponse {
    private boolean valid;
    private String promoCode;
    private String promoType;
    private Double promoValue;
    private Double discountAmount;
    private Double finalAmount;
    private String message;

    public static PromoCodeValidationResponse valid(
            String promoCode,
            String promoType,
            Double promoValue,
            Double discountAmount,
            Double finalAmount,
            String message) {
        return new PromoCodeValidationResponse(
                true,
                promoCode,
                promoType,
                promoValue,
                discountAmount,
                finalAmount,
                message);
    }

    public static PromoCodeValidationResponse invalid(String message) {
        return new PromoCodeValidationResponse(
                false,
                null,
                null,
                null,
                0.0,
                0.0,
                message);
    }
}
