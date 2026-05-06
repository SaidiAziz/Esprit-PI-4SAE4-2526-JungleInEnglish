// entities/Payment.java
package esprit.tn.junglepayments.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   /* @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long eventId;// ✅ Référence vers jungle-events (pas de FK directe)*/
   @Column(nullable = true)  // ← plus @NotNull
   private Long eventId;     // garde pour compatibilité collègue

  @Column(nullable = true)
  private Long bookingId;

    private String participantName;
    @NotNull
    private String participantEmail;

    @Positive
    private Double amount;

    private Double originalAmount;

    private Double discountAmount;

    private String promoCode;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private LocalDateTime paymentDate = LocalDateTime.now();

    private String reference;// référence unique du paiement
    private Long participantId;

    private Long sessionId;

    @Column(length = 50)
    private String participationStatus;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;
}
