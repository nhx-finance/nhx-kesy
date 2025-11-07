package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.InvalidOtpException;
import com.javaguy.nhx.model.entity.Otp;
import com.javaguy.nhx.repository.OtpRepository;
import com.javaguy.nhx.util.AppConstants;
import com.javaguy.nhx.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

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

        sendEmail(email, otpCode);
        log.info("✅ OTP sent to {}: {}", email, otpCode);
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        Otp otp = otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(
                        email, otpCode, LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP."));

        otp.setUsed(true);
        otpRepository.save(otp);
        log.info("✅ OTP for {} successfully verified.", email);
    }

    private void sendEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("NHX KESY OTP Verification");
            message.setText(String.format(
                    "Your OTP for NHX KESY is: %s\n\n" +
                            "This code is valid for %d minutes.\n\n" +
                            "If you didn't request this code, please ignore this email.",
                    otp, AppConstants.OTP_EXPIRY_MINUTES
            ));
            mailSender.send(message);
            log.info("📧 OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send OTP email.", e);
        }
    }
}