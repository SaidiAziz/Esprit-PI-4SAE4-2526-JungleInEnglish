package esprit.tn.junglepayments.services;

import esprit.tn.junglepayments.entities.*;
import esprit.tn.junglepayments.repositories.LoyaltyAccountRepository;
import esprit.tn.junglepayments.repositories.PromoCodeRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoyaltyServiceImpl implements LoyaltyService {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    /**
     * Règle des points : 1 événement payé = 1 point
     * Tiers (packs) :
     *   BRONZE  0 – 2 événements
     *   SILVER  3 – 5 événements
     *   GOLD    6+ événements
     *
     * Jalons récompensés (une seule fois chacun) :
     *   2 events → code perso 10% valable 1 mois, usage unique
     *   4 events → code perso 30% valable 1 mois, usage unique
     *   6 events → code perso 40% valable 1 mois, usage unique
     */
    @Override
    public void creditPoints(Payment payment) {
        if (payment == null || payment.getParticipantEmail() == null) return;

        String email = payment.getParticipantEmail();
        int earned = 1; // 1 événement payé = 1 point, peu importe le montant

        // Récupérer ou créer le compte
        LoyaltyAccount account = loyaltyAccountRepository
                .findByParticipantEmail(email)
                .orElseGet(() -> {
                    LoyaltyAccount newAcc = new LoyaltyAccount();
                    newAcc.setParticipantEmail(email);
                    newAcc.setParticipantId(payment.getParticipantId());
                    newAcc.setParticipantName(payment.getParticipantName());
                    return newAcc;
                });

        // Mettre à jour les totaux
        account.setTotalPoints(account.getTotalPoints() + earned);
        if (payment.getAmount() != null) {
            account.setTotalSpent(account.getTotalSpent() + payment.getAmount());
        }
        account.setLastUpdated(LocalDateTime.now());

        // Recalculer le tier
        account.setTier(computeTier(account.getTotalPoints()));

        // Vérifier et récompenser les jalons
        rewardMilestones(account);

        loyaltyAccountRepository.save(account);

        System.out.println("Loyalty: +1 event → total=" + account.getTotalPoints()
                + " events, tier=" + account.getTier() + " [" + email + "]");
    }

    @Override
    public LoyaltyAccount getAccount(String email) {
        return loyaltyAccountRepository.findByParticipantEmail(email).orElse(null);
    }

    @Override
    public List<PromoCode> getLoyaltyCodesByUser(String email) {
        return promoCodeRepository.findByOwnerEmail(email);
    }

    @Override
    public List<PromoCode> getAllLoyaltyCodes() {
        return promoCodeRepository.findByOwnerEmailIsNotNull();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────

    private LoyaltyTier computeTier(int events) {
        if (events >= 6) return LoyaltyTier.GOLD;
        if (events >= 3) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }

    private void rewardMilestones(LoyaltyAccount account) {
        int events = account.getTotalPoints();

        if (events >= 2 && !Boolean.TRUE.equals(account.getMilestone100Rewarded())) {
            generatePersonalPromo(account.getParticipantEmail(), 10.0, "FIDEL2");
            account.setMilestone100Rewarded(true);
            System.out.println("Loyalty milestone 2 events → code 10% généré pour " + account.getParticipantEmail());
        }
        if (events >= 4 && !Boolean.TRUE.equals(account.getMilestone300Rewarded())) {
            generatePersonalPromo(account.getParticipantEmail(), 30.0, "FIDEL4");
            account.setMilestone300Rewarded(true);
            System.out.println("Loyalty milestone 4 events → code 30% généré pour " + account.getParticipantEmail());
        }
        if (events >= 6 && !Boolean.TRUE.equals(account.getMilestone700Rewarded())) {
            generatePersonalPromo(account.getParticipantEmail(), 40.0, "FIDEL6");
            account.setMilestone700Rewarded(true);
            System.out.println("Loyalty milestone 6 events → code 40% généré pour " + account.getParticipantEmail());
        }
    }

    private void generatePersonalPromo(String email, Double discountPercent, String prefix) {
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String code = prefix + "-" + uniquePart;

        PromoCode promo = new PromoCode();
        promo.setCode(code);
        promo.setType(PromoCodeType.PERCENTAGE);
        promo.setValue(discountPercent);
        promo.setMaxUses(1);
        promo.setUsesCount(0);
        promo.setEventId(null);
        promo.setMinAmount(null);
        promo.setOwnerEmail(email);
        promo.setExpirationDate(LocalDate.now().plusMonths(1));

        promoCodeRepository.save(promo);
        System.out.println("Code promo fidélité créé : " + code + " (" + discountPercent + "%) pour " + email);
    }
}
