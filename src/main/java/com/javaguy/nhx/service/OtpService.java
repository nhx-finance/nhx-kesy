package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.EmailServiceException;
import com.javaguy.nhx.exception.InvalidOtpException;
import com.javaguy.nhx.model.entity.Otp;
import com.javaguy.nhx.repository.OtpRepository;
import com.javaguy.nhx.util.AppConstants;
import com.javaguy.nhx.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public void sendOtp(String email) {
        String otpCode = OtpGenerator.generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(AppConstants.OTP_EXPIRY_MINUTES);

        Otp otp = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .expiryTime(expiryTime)
                .used(false)
                .build();
        otpRepository.save(otp);

        try {
            emailNotificationService.sendOtpEmail(email, otpCode);
            log.info("✅ OTP sent to {}", email);
        } catch (EmailServiceException e) {
            log.error("❌ Failed to send OTP email to {}", email, e);
            throw new EmailServiceException("Failed to send OTP email", e);
        }
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        Otp otp = otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
                        email, otpCode, LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));

        otp.setUsed(true);
        otpRepository.save(otp);
        log.info("✅ OTP for {} successfully verified", email);
    }
}