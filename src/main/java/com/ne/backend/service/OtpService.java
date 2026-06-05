package com.ne.backend.service;

import com.ne.backend.entity.Otp;
import com.ne.backend.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Transactional
    public String generateAndSendOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        otpRepository.deleteByEmail(email);

        String otpCode = generateRandomOtp();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        Otp otp = Otp.builder()
                .email(email)
                .code(otpCode)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        otpRepository.save(otp);

        emailService.sendEmail(
                email,
                "Your OTP Code",
                "Your OTP code is: " + otpCode +
                        "\n\nThis code will expire in " +
                        otpExpirationMinutes + " minutes."
        );

        return otpCode;
    }

    public boolean validateOtp(String email, String code) {
        log.info("Validating OTP for email: {}", email);

        Otp otp = otpRepository.findByEmailAndCodeAndUsedFalse(email, code)
                .orElse(null);

        if (otp == null) {
            log.warn("Invalid OTP or OTP not found for email: {}", email);
            return false;
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            return false;
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("OTP validated successfully for email: {}", email);
        return true;
    }

    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
}
