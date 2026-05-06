package tn.esprit.administrationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);

            mailSender.send(msg);

            System.out.println("✅ MAIL SENT to=" + to + " subject=" + subject);
        } catch (Exception ex) {
            System.out.println("❌ MAIL ERROR to=" + to + " subject=" + subject + " => " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}