package exp.usermicroservice.Controllers;

import exp.usermicroservice.DTO.Request.ForgotPasswordRequest;
import exp.usermicroservice.DTO.Request.LoginRequest;
import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.DTO.Request.ResetPasswordRequest;
import exp.usermicroservice.DTO.Response.LoginResponse;
import exp.usermicroservice.DTO.Response.UserResponse;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Mapper.UserMapper;
import exp.usermicroservice.Security.JwtUtil;
import exp.usermicroservice.Services.PasswordResetService;
import exp.usermicroservice.Services.TwoFactorService;
import exp.usermicroservice.Services.UserServiceI;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final UserServiceI userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private final TwoFactorService twoFactorService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2FA enabled → don't return JWT yet
        if (user.isTwoFactorEnabled()) {
            if (user.getTwoFactorMethod() == exp.usermicroservice.Entities.TwoFactorMethod.EMAIL) {
                twoFactorService.sendEmailOtp(user);
            }
            return ResponseEntity.ok(LoginResponse.builder()
                    .requires2FA(true)
                    .twoFactorMethod(user.getTwoFactorMethod().name())
                    .email(user.getEmail())
                    .build());
        }

        // No 2FA → return JWT directly
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .requires2FA(false)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {
        if (userService.getUserByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = userMapper.toEntity(request);
        User created = userService.createUser(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(created));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.RequestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "If this email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.ResetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}
