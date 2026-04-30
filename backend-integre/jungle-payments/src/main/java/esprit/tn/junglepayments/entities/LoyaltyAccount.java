package esprit.tn.junglepayments.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_account")
@Data
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String participantEmail;

    private Long participantId;

    private String participantName;

    // Points cumulés (1 point = 1 TND payé)
    private Integer totalPoints = 0;

    // Montant total dépensé en TND
    private Double totalSpent = 0.0;

    @Enumerated(EnumType.STRING)
    private LoyaltyTier tier = LoyaltyTier.BRONZE;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Jalons déjà récompensés (évite les doublons de codes promo)
    private Boolean milestone100Rewarded = false;  // 100 pts  → code 5%
    private Boolean milestone300Rewarded = false;  // 300 pts  → code 10%
    private Boolean milestone700Rewarded = false;  // 700 pts  → code 15%
}
