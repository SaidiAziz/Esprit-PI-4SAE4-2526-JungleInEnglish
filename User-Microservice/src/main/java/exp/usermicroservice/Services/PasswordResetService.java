package exp.usermicroservice.Services;

import exp.usermicroservice.Entities.PasswordResetToken;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Repositories.PasswordResetTokenRepo;
import exp.usermicroservice.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.reset-token.expiry-minutes}")
    private int expiryMinutes;

    public void RequestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Delete existing token if any
        passwordResetTokenRepo.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(expiryMinutes));
        passwordResetTokenRepo.save(resetToken);

        // Build reset link and send email
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token, resetLink);

        log.info("Password reset email sent to {}", email);
    }

    public void ResetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepo.delete(resetToken);
            throw new RuntimeException("Token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepo.delete(resetToken);
        log.info("Password reset successfully for user {}", user.getEmail());
    }
}
