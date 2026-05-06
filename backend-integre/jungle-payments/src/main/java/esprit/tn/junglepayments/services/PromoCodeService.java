package esprit.tn.junglepayments.services;

import esprit.tn.junglepayments.DTO.PromoCodeValidationRequest;
import esprit.tn.junglepayments.DTO.PromoCodeValidationResponse;

public interface PromoCodeService {
    PromoCodeValidationResponse validatePromoCode(PromoCodeValidationRequest request);
    void incrementUses(String code);
}
