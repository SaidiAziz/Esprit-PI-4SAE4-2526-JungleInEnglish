package exp.usermicroservice.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender; // ← final is required for @RequiredArgsConstructor

    public void sendEmail(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Jungle@mail.com");
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message); // ← uncommented
            log.info("Email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
        }
    }

    public void sendWelcomeEmail(String email, String name) {
        String subject = "Welcome to Jungle!";
        String body = "Hi " + name + ",\n\nWelcome to Jungle! We're excited to have you on board.\n\nBest regards,\nThe Jungle Team";
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String name, String resetToken, String resetLink) {
        String subject = "Réinitialisation de votre mot de passe Jungle";
        String body = "Bonjour " + name + ",\n\n" +
                "Vous avez demandé une réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe:\n" +
                resetLink + "\n\n" +
                "Token: " + resetToken + "\n\n" +
                "Ce lien expire dans 24 heures.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "Cordialement,\nL'équipe Jungle";
        sendEmail(email, subject, body);
    }
}