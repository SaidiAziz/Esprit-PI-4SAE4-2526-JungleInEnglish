package esprit.tn.junglepayments.repositories;

import esprit.tn.junglepayments.entities.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByParticipantEmail(String email);
}
