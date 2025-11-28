package com.javaguy.nhx.service.auth;

import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.UnauthorizedException;
import com.javaguy.nhx.model.entity.Otp;
import com.javaguy.nhx.repository.OtpRepository;
import com.javaguy.nhx.service.email.EmailNotificationService;
import com.javaguy.nhx.util.AppConstants;
import com.javaguy.nhx.util.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;
    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private OtpService otpService;

    private String email = "test@example.com";
    private String otpCode = "123456";
    private Otp otpEntity;

    @BeforeEach
    void setUp() {
        otpEntity = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(AppConstants.OTP_EXPIRY_MINUTES))
                .used(false)
                .build();
    }

    @Test
    void sendOtp_Success() {
        try (MockedStatic<OtpGenerator> mockedStatic = mockStatic(OtpGenerator.class)) {
            mockedStatic.when(OtpGenerator::generateOtp).thenReturn(otpCode);

            when(otpRepository.save(any(Otp.class))).thenReturn(otpEntity);
            doNothing().when(emailNotificationService).sendOtpEmail(eq(email), eq(otpCode));

            otpService.sendOtp(email);

            verify(otpRepository, times(1)).save(any(Otp.class));
            verify(emailNotificationService, times(1)).sendOtpEmail(eq(email), eq(otpCode));
        }
    }

    @Test
    void sendOtp_EmailNotificationFails_ThrowsInternalServerException() {
        try (MockedStatic<OtpGenerator> mockedStatic = mockStatic(OtpGenerator.class)) {
            mockedStatic.when(OtpGenerator::generateOtp).thenReturn(otpCode);

            when(otpRepository.save(any(Otp.class))).thenReturn(otpEntity);
            doThrow(new RuntimeException("Email send failed")).when(emailNotificationService).sendOtpEmail(eq(email),
                    eq(otpCode));

            assertThrows(InternalServerException.class, () -> otpService.sendOtp(email));

            verify(otpRepository, times(1)).save(any(Otp.class));
            verify(emailNotificationService, times(1)).sendOtpEmail(eq(email), eq(otpCode));
        }
    }

    @Test
    void verifyOtp_Success() {
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpEntity));
        when(otpRepository.save(any(Otp.class))).thenReturn(otpEntity);

        assertDoesNotThrow(() -> otpService.verifyOtp(email, otpCode));

        assertTrue(otpEntity.getUsed());
        verify(otpRepository, times(1)).findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class));
        verify(otpRepository, times(1)).save(otpEntity);
    }

    @Test
    void verifyOtp_InvalidOtp_ThrowsUnauthorizedException() {
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq("wrongOtp"),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> otpService.verifyOtp(email, "wrongOtp"));

        verify(otpRepository, times(1)).findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq("wrongOtp"),
                any(LocalDateTime.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verifyOtp_ExpiredOtp_ThrowsUnauthorizedException() {
        otpEntity.setExpiryTime(LocalDateTime.now().minusMinutes(1)); // Make it expired
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty()); // findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter should return
                                               // empty for expired OTP

        assertThrows(UnauthorizedException.class, () -> otpService.verifyOtp(email, otpCode));

        verify(otpRepository, times(1)).findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verifyOtp_AlreadyUsedOtp_ThrowsUnauthorizedException() {
        otpEntity.setUsed(true); // Make it used
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty()); // findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter should return
                                               // empty for used OTP

        assertThrows(UnauthorizedException.class, () -> otpService.verifyOtp(email, otpCode));

        verify(otpRepository, times(1)).findByEmailAndOtpCodeAndUsedFalseAndExpiryTimeAfter(eq(email), eq(otpCode),
                any(LocalDateTime.class));
        verify(otpRepository, never()).save(any(Otp.class));
    }
}
