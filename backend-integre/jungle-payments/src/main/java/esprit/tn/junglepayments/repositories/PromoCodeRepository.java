package esprit.tn.junglepayments.repositories;

import esprit.tn.junglepayments.entities.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCaseAndEventId(String code, Long eventId);
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    List<PromoCode> findByOwnerEmail(String ownerEmail);
    List<PromoCode> findByOwnerEmailIsNotNull();
}
