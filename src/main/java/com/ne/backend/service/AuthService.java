package com.ne.backend.service;

import com.ne.backend.config.JwtUtil;
import com.ne.backend.dto.LoginRequest;
import com.ne.backend.dto.LoginResponse;
import com.ne.backend.dto.RegisterRequest;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.User;
import com.ne.backend.enums.Role;
import com.ne.backend.enums.UserStatus;
import com.ne.backend.repository.CustomerRepository;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpService otpService;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registering new customer with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        long count = customerRepository.count() + 1;

        // Create Customer entity (extends User) with CUSTOMER role
        Customer customer = new Customer();
        customer.setCode(String.format("USR-%03d", count));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setMobile(request.getMobile());
        customer.setStatus(UserStatus.ACTIVE);
        customer.setRoles(Set.of(Role.ROLE_CUSTOMER));
        customer.setNationalId("0000000000000000"); // Temporary, will be updated by customer service
        customer.setAddress("To be updated");
        customer.setCustomerStatus(com.ne.backend.enums.CustomerStatus.ACTIVE);
        customer.setSurplus(java.math.BigDecimal.ZERO);

        customerRepository.save(customer);
        log.info("Customer registered successfully with email: {}", request.getEmail());

        emailService.sendEmail(
                customer.getEmail(),
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

        // Try to find customer first, then fall back to user
        User user = customerRepository.findByEmail(request.getEmail())
                .map(customer -> (User) customer)
                .orElseGet(() -> userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found")));

        // Generate and send OTP
        otpService.generateAndSendOtp(user.getEmail());
        log.info("OTP sent to email: {}", user.getEmail());
    }

    @Transactional
    public LoginResponse verifyOtpAndLogin(String email, String otpCode) {
        log.info("Verifying OTP for email: {}", email);

        if (!otpService.validateOtp(email, otpCode)) {
            log.warn("Invalid OTP provided for email: {}", email);
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Try to find customer first, then fall back to user
        User user = customerRepository.findByEmail(email)
                .map(customer -> (User) customer)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        // Ensure roles are loaded
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.warn("User has no roles, assigning CUSTOMER role");
            user.setRoles(Set.of(Role.ROLE_CUSTOMER));
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user);

        log.info("Login successful for email: {} with roles: {}", email, user.getRoles());

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
