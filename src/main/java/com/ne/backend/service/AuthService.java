package com.ne.backend.service;

import com.ne.backend.config.JwtUtil;
import com.ne.backend.dto.LoginRequest;
import com.ne.backend.dto.LoginResponse;
import com.ne.backend.dto.RegisterRequest;
import com.ne.backend.entity.User;
import com.ne.backend.enums.Role;
import com.ne.backend.enums.UserStatus;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpService otpService;

    public void register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        long count = userRepository.count() + 1;

        User user = User.builder()
                .code(String.format("USR-%03d", count))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .build();

        userRepository.save(user);
        log.info("User registered successfully with email: {}", request.getEmail());

        emailService.sendEmail(
                user.getEmail(),
                "Welcome",
                "Your account has been created successfully"
        );
    }

    public void initiateLogin(LoginRequest request) {
        log.info("Initiating login for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate and send OTP
        otpService.generateAndSendOtp(user.getEmail());
        log.info("OTP sent to email: {}", user.getEmail());
    }

    public LoginResponse verifyOtpAndLogin(String email, String otpCode) {
        log.info("Verifying OTP for email: {}", email);

        if (!otpService.validateOtp(email, otpCode)) {
            log.warn("Invalid OTP provided for email: {}", email);
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        log.info("Login successful for email: {}", email);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
