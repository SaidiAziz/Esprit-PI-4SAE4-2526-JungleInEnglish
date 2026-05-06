package esprit.tn.junglepayments.services;

import esprit.tn.junglepayments.entities.LoyaltyAccount;
import esprit.tn.junglepayments.entities.Payment;
import esprit.tn.junglepayments.entities.PromoCode;

import java.util.List;

public interface LoyaltyService {
    void creditPoints(Payment payment);
    LoyaltyAccount getAccount(String email);
    List<PromoCode> getLoyaltyCodesByUser(String email);
    List<PromoCode> getAllLoyaltyCodes();
}
