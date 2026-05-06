package esprit.tn.junglepayments.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ParticipationRequest {
    private Long sessionId;
    private String userEmail;
    private Long userId;
    private Double amountPaid;
    private String paymentMethod;    // doit valoir exactement "CREDIT_CARD" (comme l'enum PaymentMethod)
    private String paymentStatus;    // doit valoir exactement "PAID" (comme l'enum PaymentStatus)
    private String status;           // doit valoir exactement "REGISTERED" (comme l'enum ParticipationStatus)
    private LocalDate registrationDate;
}
