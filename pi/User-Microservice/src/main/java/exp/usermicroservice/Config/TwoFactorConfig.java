package exp.usermicroservice.Config;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class TwoFactorConfig {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_LENGTH = 32;

    @Bean
    public SecretGenerator secretGenerator() {
        // Generate RFC-3548 compatible Base32 secrets for authenticator apps.
        SecureRandom secureRandom = new SecureRandom();
        return () -> {
            StringBuilder sb = new StringBuilder(SECRET_LENGTH);
            for (int i = 0; i < SECRET_LENGTH; i++) {
                int index = secureRandom.nextInt(BASE32_ALPHABET.length());
                sb.append(BASE32_ALPHABET.charAt(index));
            }
            return sb.toString();
        };
    }

    @Bean
    public QrDataFactory qrDataFactory() {
        return new QrDataFactory(HashingAlgorithm.SHA1, 6, 30);
    }

    @Bean
    public QrGenerator qrGenerator() {
        return new ZxingPngQrGenerator();
    }

    @Bean
    public CodeVerifier codeVerifier() {
        return new DefaultCodeVerifier(
                new DefaultCodeGenerator(HashingAlgorithm.SHA1),
                new SystemTimeProvider()
        );
    }
}
