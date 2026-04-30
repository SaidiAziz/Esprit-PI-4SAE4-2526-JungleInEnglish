// services/PaymentService.java
package esprit.tn.junglepayments.services;

import esprit.tn.junglepayments.DTO.EventResponse;
import esprit.tn.junglepayments.entities.Payment;
import java.util.List;

public interface PaymentService {
    Payment createPayment(Payment payment);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    Payment updatePayment(Long id, Payment payment);
    void deletePayment(Long id);
    Payment updatePaymentStatus(Long id, String status);
    EventResponse getEventDetails(Long eventId);  // ✅ Appel via Feign
    Payment confirmStripePayment(String paymentIntentId, Payment payment);
}