package com.ne.backend.config;

import com.ne.backend.entity.User;
import com.ne.backend.enums.Role;
import com.ne.backend.enums.UserStatus;
import com.ne.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        log.info("Checking if data seeding is required...");

        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            return;
        }

        log.info("Starting data seeding...");

        User admin = User.builder()
                .code("EMP-001")
                .firstName("System")
                .lastName("Admin")
                .email("aimable.kwizera14@gmail.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .mobile("0700000001")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        User operator = User.builder()
                .code("EMP-002")
                .firstName("System")
                .lastName("Operator")
                .email("operator@app.com")
                .password(passwordEncoder.encode("Operator@1234"))
                .mobile("0700000002")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_OPERATOR))
                .build();

        User finance = User.builder()
                .code("EMP-003")
                .firstName("System")
                .lastName("Finance")
                .email("finance@app.com")
                .password(passwordEncoder.encode("Finance@1234"))
                .mobile("0700000003")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_FINANCE))
                .build();

        User customer = User.builder()
                .code("EMP-004")
                .firstName("Normal")
                .lastName("Customer")
                .email("customer@app.com")
                .password(passwordEncoder.encode("Customer@1234"))
                .mobile("0700000004")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .build();

        userRepository.save(admin);
        userRepository.save(operator);
        userRepository.save(finance);
        userRepository.save(customer);

        log.info("Data seeding completed successfully. Created 4 default users.");
    }
}