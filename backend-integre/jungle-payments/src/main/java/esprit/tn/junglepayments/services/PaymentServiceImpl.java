// services/PaymentServiceImpl.java
package esprit.tn.junglepayments.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import esprit.tn.junglepayments.DTO.*;
import esprit.tn.junglepayments.repositories.EventFeignClient;
import esprit.tn.junglepayments.DTO.EventResponse;
import esprit.tn.junglepayments.entities.Payment;
import esprit.tn.junglepayments.entities.PaymentStatus;
import esprit.tn.junglepayments.repositories.EventFeignClient;
import esprit.tn.junglepayments.repositories.PaymentRepository;
import esprit.tn.junglepayments.repositories.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private EventFeignClient eventFeignClient;

    @Autowired
    private LoyaltyService loyaltyService;


    @Override
    public Payment createPayment(Payment payment) {
        // ✅ 1. Vérifier que l'événement existe
        EventResponse event = eventFeignClient.getEventById(payment.getEventId());

        if (payment.getAmount() == null) {
            payment.setAmount(event.getPrice());
        }

        // ✅ 2. Récupérer le vrai userId depuis User MS
        try {
            UserDTO user = userFeignClient.getUserByEmail(payment.getParticipantEmail());
            payment.setParticipantId(user.getId()); // ← id=3 au lieu de NULL
            if (payment.getParticipantName() == null) {
                payment.setParticipantName(
                        user.getFirstName() + " " + user.getLastName()
                );
            }
            System.out.println("✅ User résolu : id=" + user.getId());
        } catch (Exception e) {
            System.err.println("⚠️ User MS injoignable : " + e.getMessage());
        }

        // ✅ 2. Récupérer les infos de participation depuis jungle-events

        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING); // Par défaut si vide
        }
        // ✅ 4. Référence unique + statut
        payment.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());


        return paymentRepository.save(payment);
    }


    @Override
    public Payment confirmStripePayment(String paymentIntentId, Payment payment) {

        // ✅ Étape 1 : vérifier Stripe
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            payment.setStatus("succeeded".equals(intent.getStatus()) ?
                    PaymentStatus.PAID : PaymentStatus.FAILED);
            payment.setStripePaymentIntentId(paymentIntentId);
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            System.err.println("❌ Stripe error: " + e.getMessage());
        }

        // ✅ Étape 2 : résoudre userId depuis User MS
        try {
            UserDTO user = userFeignClient.getUserByEmail(payment.getParticipantEmail());
            payment.setParticipantId(user.getId());
            if (payment.getParticipantName() == null) {
                payment.setParticipantName(user.getFirstName() + " " + user.getLastName());
            }
            System.out.println("✅ User résolu : id=" + user.getId());
        } catch (Exception e) {
            System.err.println("⚠️ User MS injoignable : " + e.getMessage());
        }

        // ✅ Étape 3 : récupérer le prix si amount null
        try {
            EventResponse event = eventFeignClient.getEventById(payment.getEventId());
            if (payment.getAmount() == null) {
                payment.setAmount(event.getPrice());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Event MS injoignable : " + e.getMessage());
        }

        // ✅ Étape 4 : générer référence unique
        payment.setReference("PAY-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());

        // ✅ Étape 5 : UN SEUL save
        Payment saved = paymentRepository.save(payment);

        // ✅ Étape 6 : si PAID → créditer les points de fidélité
        if (PaymentStatus.PAID.equals(saved.getStatus())) {
            try {
                loyaltyService.creditPoints(saved);
            } catch (Exception e) {
                System.err.println("⚠️ Loyalty service error: " + e.getMessage());
            }
        }

        // ✅ Étape 7 : si PAID → créer participation dans jungle-events
        if (PaymentStatus.PAID.equals(saved.getStatus())) {
            try {
                ParticipationRequest req = new ParticipationRequest();
                req.setSessionId(payment.getSessionId());
                req.setUserEmail(payment.getParticipantEmail());
                req.setUserId(payment.getParticipantId());
                req.setAmountPaid(payment.getAmount());
                req.setPaymentMethod(
                        payment.getPaymentMethod() != null ?
                                payment.getPaymentMethod().toString() : "CREDIT_CARD"
                );
                req.setPaymentStatus("PAID");
                req.setStatus("REGISTERED");
                req.setRegistrationDate(LocalDate.now());

                RegistrationResponse registration =
                        eventFeignClient.createParticipation(req);

                // ✅ Mettre à jour avec sessionId + participation_status
                saved.setSessionId(registration.getSessionId());
                saved.setParticipationStatus(registration.getStatus());
                paymentRepository.save(saved); // ← seul update, pas de double insert

                System.out.println("✅ Participation créée : id=" + registration.getId());

            } catch (Exception e) {
                System.err.println("⚠️ Impossible de créer la participation : " + e.getMessage());
            }
        }

        return saved;
    }
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + id));
    }
    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));

        // Mise à jour uniquement des champs modifiables
        if (payment.getAmount() != null) existing.setAmount(payment.getAmount());
        if (payment.getStatus() != null) existing.setStatus(payment.getStatus());
        if (payment.getPaymentMethod() != null) existing.setPaymentMethod(payment.getPaymentMethod());
        if (payment.getParticipantName() != null) existing.setParticipantName(payment.getParticipantName());
        if (payment.getParticipantEmail() != null) existing.setParticipantEmail(payment.getParticipantEmail());

        return paymentRepository.save(existing);
    }
    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found: " + id);
        }
        paymentRepository.deleteById(id);
    }
    @Override
    public Payment updatePaymentStatus(Long id, String status) {
        Payment payment = getPaymentById(id);
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        return paymentRepository.save(payment);
    }

    @Override
    public EventResponse getEventDetails(Long eventId) {
        // ✅ Appel direct à jungle-events via Feign
        return eventFeignClient.getEventById(eventId);
    }
}