package com.javaguy.nhx.repository;

import com.javaguy.nhx.model.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
            String email, String otpCode, LocalDateTime currentTime);

    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);
}
