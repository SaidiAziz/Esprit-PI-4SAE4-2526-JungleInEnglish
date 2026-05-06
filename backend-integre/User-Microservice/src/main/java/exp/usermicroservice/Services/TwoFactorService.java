package exp.usermicroservice.Services;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import exp.usermicroservice.DTO.Response.TotpSetupResponse;
import exp.usermicroservice.Entities.TwoFactorCode;
import exp.usermicroservice.Entities.TwoFactorMethod;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Repositories.TwoFactorCodeRepository;
import exp.usermicroservice.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TwoFactorService {

    private final TwoFactorCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    // ─── EMAIL OTP ───────────────────────────────────────────

    public void sendEmailOtp(User user) {
        codeRepository.deleteByUser(user);

        String code = String.format("%06d", new Random().nextInt(999999));

        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setCode(code);
        twoFactorCode.setUser(user);
        twoFactorCode.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        codeRepository.save(twoFactorCode);

        emailService.send2FAEmail(user.getEmail(), user.getFirstName(), code);
        log.info("2FA OTP sent to {}", user.getEmail());
    }

    public boolean verifyEmailOtp(User user, String inputCode) {
        TwoFactorCode stored = codeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No OTP found. Please login again."));

        if (stored.isExpired()) {
            codeRepository.delete(stored);
            throw new RuntimeException("OTP has expired. Please login again.");
        }

        if (!stored.getCode().equals(inputCode)) {
            return false;
        }

        codeRepository.delete(stored);
        return true;
    }

    // ─── TOTP (Google Authenticator) ─────────────────────────

    public TotpSetupResponse setupTotp(User user) throws QrGenerationException {
        String secret = secretGenerator.generate();

        QrData qrData = qrDataFactory.newBuilder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("Jungle App")
                .build();

        String qrCodeBase64 = "data:image/png;base64," +
                Base64.getEncoder().encodeToString(qrGenerator.generate(qrData));

        user.setTotpSecret(secret);
        userRepository.save(user);

        return new TotpSetupResponse(secret, qrCodeBase64);
    }

    public boolean verifyTotp(User user, String inputCode) {
        if (user.getTotpSecret() == null) {
            throw new RuntimeException("TOTP not set up for this user.");
        }
        return codeVerifier.isValidCode(user.getTotpSecret(), inputCode);
    }

    // ─── ENABLE / DISABLE ────────────────────────────────────

    public void enable2FA(User user, TwoFactorMethod method) {
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(method);
        userRepository.save(user);
    }

    public void disable2FA(User user) {
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(null);
        user.setTotpSecret(null);
        userRepository.save(user);
        codeRepository.deleteByUser(user);
    }
}

