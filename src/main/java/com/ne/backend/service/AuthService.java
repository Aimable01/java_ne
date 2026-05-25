package com.ne.backend.service;

import com.ne.backend.dto.LoginRequest;
import com.ne.backend.dto.LoginResponse;
import com.ne.backend.dto.RegisterRequest;
import com.ne.backend.entity.User;
import com.ne.backend.enums.Role;
import com.ne.backend.enums.UserStatus;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        long count = userRepository.count() + 1;

        User user = User.builder()
                .code(String.format("EMP-%03d", count))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Welcome",
                "Your account has been created successfully"
        );
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

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
