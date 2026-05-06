package esprit.tn.jungleevents.services;

public interface EmailService {
    void sendRibEmail(String toEmail, String eventTitle, String sessionDate, double amount);
    void sendCertificateEmail(String email, String participantName, String eventTitle, String sessionDate);

}
