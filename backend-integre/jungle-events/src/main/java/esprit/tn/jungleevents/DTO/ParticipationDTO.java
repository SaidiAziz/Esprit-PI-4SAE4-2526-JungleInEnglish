package esprit.tn.jungleevents.DTO;


import esprit.tn.jungleevents.entities.ParticipationStatus;
import esprit.tn.jungleevents.entities.PaymentMethod;
import esprit.tn.jungleevents.entities.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ParticipationDTO {

    private Long id;
    private LocalDate registrationDate;
    private ParticipationStatus status;
    private PaymentStatus paymentStatus;
    private Double amountPaid;
    private PaymentMethod paymentMethod;
    private String userName;
    private String userEmail;
    private String eventTitle;
    private Long userId;
    private Long sessionId;
}
