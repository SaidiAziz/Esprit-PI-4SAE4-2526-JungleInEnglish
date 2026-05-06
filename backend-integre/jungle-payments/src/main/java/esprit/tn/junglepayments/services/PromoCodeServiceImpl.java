package esprit.tn.junglepayments.services;

import esprit.tn.junglepayments.DTO.PromoCodeValidationRequest;
import esprit.tn.junglepayments.DTO.PromoCodeValidationResponse;
import esprit.tn.junglepayments.entities.PromoCode;
import esprit.tn.junglepayments.entities.PromoCodeType;
import esprit.tn.junglepayments.repositories.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PromoCodeServiceImpl implements PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Override
    public PromoCodeValidationResponse validatePromoCode(PromoCodeValidationRequest request) {
        String code = request.getPromoCode();
        if (code == null || code.isBlank()) {
            return PromoCodeValidationResponse.invalid("Code promo requis.");
        }

        System.out.println("Validating promo code: " + code + " for eventId: " + request.getEventId());

        PromoCode promoCode;
        if (request.getEventId() != null) {
            promoCode = promoCodeRepository.findByCodeIgnoreCaseAndEventId(code.trim().toUpperCase(), request.getEventId()).orElse(null);
            if (promoCode == null) {
                System.out.println("Promo not found with eventId, trying global");
                promoCode = promoCodeRepository.findByCodeIgnoreCase(code.trim().toUpperCase()).orElse(null);
            }
        } else {
            promoCode = promoCodeRepository.findByCodeIgnoreCase(code.trim().toUpperCase()).orElse(null);
        }

        if (promoCode == null) {
            System.out.println("Promo code not found: " + code);
            return PromoCodeValidationResponse.invalid("Code promo invalide ou non applicable à cet événement.");
        }

        System.out.println("Found promo: " + promoCode.getCode() + " type: " + promoCode.getType() + " value: " + promoCode.getValue() + " eventId: " + promoCode.getEventId());

        if (promoCode.getExpirationDate() != null && promoCode.getExpirationDate().isBefore(LocalDate.now())) {
            return PromoCodeValidationResponse.invalid("Ce code promo est expiré.");
        }

        if (promoCode.getMaxUses() != null && promoCode.getUsesCount() != null && promoCode.getUsesCount() >= promoCode.getMaxUses()) {
            return PromoCodeValidationResponse.invalid("Ce code promo a déjà été utilisé trop de fois.");
        }

        Double amount = request.getAmount() != null ? request.getAmount() : 0.0;
        if (promoCode.getMinAmount() != null && amount < promoCode.getMinAmount()) {
            return PromoCodeValidationResponse.invalid(
                    "Montant minimum de " + promoCode.getMinAmount() + " TND requis.");
        }

        Double discount = 0.0;
        String label;
        if (promoCode.getType() == PromoCodeType.PERCENTAGE) {
            discount = amount * promoCode.getValue() / 100.0;
            label = String.format("%s%% de réduction", promoCode.getValue().intValue());
        } else {
            discount = promoCode.getValue();
            label = String.format("%s TND de réduction", promoCode.getValue().intValue());
        }

        if (discount > amount) {
            discount = amount;
        }

        Double total = amount - discount;
        if (total < 0) {
            total = 0.0;
        }

        return PromoCodeValidationResponse.valid(
                promoCode.getCode(),
                promoCode.getType().name(),
                promoCode.getValue(),
                discount,
                total,
                label);
    }

    @Override
    public void incrementUses(String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code.trim().toUpperCase()).orElse(null);
        if (promoCode != null) {
            promoCode.setUsesCount(promoCode.getUsesCount() + 1);
            promoCodeRepository.save(promoCode);
        }
    }
}
