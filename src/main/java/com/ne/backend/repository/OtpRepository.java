package com.ne.backend.repository;

import com.ne.backend.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmailAndCodeAndUsedFalse(String email, String code);

    void deleteByEmail(String email);
}
