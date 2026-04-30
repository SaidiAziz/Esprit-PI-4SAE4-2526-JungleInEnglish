package esprit.tn.jungleevents.DTO;

import esprit.tn.jungleevents.entities.PaymentMethod;
import esprit.tn.jungleevents.entities.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long eventId;
    private String participantName;
    private String participantEmail;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Long participantId;
    private Long sessionId;
}