package exp.usermicroservice.Controllers;

import dev.samstevens.totp.exceptions.QrGenerationException;
import exp.usermicroservice.DTO.Request.Verify2FARequest;
import exp.usermicroservice.DTO.Response.TotpSetupResponse;
import exp.usermicroservice.Entities.TwoFactorMethod;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Repositories.UserRepository;
import exp.usermicroservice.Security.JwtUtil;
import exp.usermicroservice.Services.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ─── Verify code after login ──────────────────────────────
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FARequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = switch (user.getTwoFactorMethod()) {
            case EMAIL -> twoFactorService.verifyEmailOtp(user, request.getCode());
            case TOTP  -> twoFactorService.verifyTotp(user, request.getCode());
        };

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired code."));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // ─── Setup TOTP (get QR code) ─────────────────────────────
    @PostMapping("/2fa/setup-totp")
    public ResponseEntity<TotpSetupResponse> setupTotp(@RequestBody Map<String, String> body)
            throws QrGenerationException {
        User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(twoFactorService.setupTotp(user));
    }

    // ─── Enable 2FA (after user confirms setup) ───────────────
    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2FA(@RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new RuntimeException("User not found"));
        TwoFactorMethod method = TwoFactorMethod.valueOf(body.get("method").toUpperCase());

        if (method == TwoFactorMethod.TOTP) {
            boolean valid = twoFactorService.verifyTotp(user, body.get("code"));
            if (!valid) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid TOTP code. Setup failed."));
        }

        twoFactorService.enable2FA(user, method);
        return ResponseEntity.ok(Map.of("message", "2FA enabled successfully."));
    }

    // ─── Disable 2FA ──────────────────────────────────────────
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA(@RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new RuntimeException("User not found"));
        twoFactorService.disable2FA(user);
        return ResponseEntity.ok(Map.of("message", "2FA disabled successfully."));
    }
}

