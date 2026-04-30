package esprit.tn.junglepayments.repositories;

import esprit.tn.junglepayments.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEventId(Long eventId);
    List<Payment> findByParticipantEmail(String email);
}
