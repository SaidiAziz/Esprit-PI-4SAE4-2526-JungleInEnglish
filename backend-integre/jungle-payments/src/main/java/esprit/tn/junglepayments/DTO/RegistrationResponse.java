package esprit.tn.junglepayments.DTO;

import lombok.Data;

@Data
public class RegistrationResponse {
    private Long id;           // ID de la registration
    private Long participantId;
    private String userName;
    private String userEmail;
    private Long sessionId;
    private String paymentStatus; // PAID
    private String status;// REGISTERED, ATTENDED, CANCELLED...
}
