package esprit.tn.junglepayments.controller;

import esprit.tn.junglepayments.entities.Payment;
import esprit.tn.junglepayments.repositories.PaymentRepository;
import esprit.tn.junglepayments.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {
    @Autowired

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;

    // ✅ Crée un paiement (appelle jungle-events en interne)
    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @PostMapping("/confirm-stripe")
    public ResponseEntity<Payment> confirmStripe(
            @RequestParam String paymentIntentId,
            @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.confirmStripePayment(paymentIntentId, payment));
    }

    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id,
                                                 @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.updatePayment(id, payment));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id,
                                                @PathVariable String status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    // ✅ Récupère les détails d'un event depuis jungle-events via Feign
    @GetMapping("/event-details/{eventId}")
    public ResponseEntity<esprit.tn.junglepayments.DTO.EventResponse> getEventDetails(@PathVariable Long eventId) {
        return ResponseEntity.ok(paymentService.getEventDetails(eventId));
    }
}

