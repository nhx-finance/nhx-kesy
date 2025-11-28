package com.javaguy.nhx.service.email;

import com.javaguy.nhx.exception.custom.EmailServiceException;
import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.model.enums.UserRole;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private User user;
    private Mint mint;
    private String email = "test@example.com";
    private String adminEmail = "admin@example.com";
    private String fromAddress = "from@example.com";
    private String appBaseUrl = "http://localhost:3000";
    private String appName = "TestApp";
    private String supportEmail = "support@example.com";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.INSTITUTIONAL_USER)
                .build();

        Wallet wallet = new Wallet();
        wallet.setWalletAddress("0x123abc");

        mint = Mint.builder()
                .id(UUID.randomUUID())
                .user(user)
                .wallet(wallet)
                .amountKes(BigDecimal.valueOf(100))
                .status(MintStatus.PENDING)
                .build();

        ReflectionTestUtils.setField(emailNotificationService, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(emailNotificationService, "fromAddress", fromAddress);
        ReflectionTestUtils.setField(emailNotificationService, "appBaseUrl", appBaseUrl);
        ReflectionTestUtils.setField(emailNotificationService, "appName", appName);
        ReflectionTestUtils.setField(emailNotificationService, "supportEmail", supportEmail);

        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendOtpEmail_Success() throws MessagingException {
        String otp = "123456";
        when(templateEngine.process(eq("email-otp"), any(Context.class))).thenReturn("<html>OTP</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.sendOtpEmail(email, otp);

        verify(templateEngine, times(1)).process(eq("email-otp"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOtpEmail_ThrowsEmailServiceExceptionOnMessagingException() {
        String otp = "123456";
        when(templateEngine.process(eq("email-otp"), any(Context.class))).thenReturn("<html>OTP</html>");
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException(new MessagingException("Test")));

        assertThrows(EmailServiceException.class, () -> emailNotificationService.sendOtpEmail(email, otp));
    }

    @Test
    void sendWelcomeEmail_Success() throws MessagingException {
        when(templateEngine.process(eq("email-welcome"), any(Context.class))).thenReturn("<html>Welcome</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.sendWelcomeEmail(user);

        verify(templateEngine, times(1)).process(eq("email-welcome"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyAdminsOnKycSubmission_Success() throws MessagingException {
        when(templateEngine.process(eq("email-admin-kyc-submission"), any(Context.class)))
                .thenReturn("<html>Admin KYC</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.notifyAdminsOnKycSubmission(user);

        verify(templateEngine, times(1)).process(eq("email-admin-kyc-submission"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyAdminsOnKycSubmission_NoAdminEmailConfigured_SkipsNotification() throws MessagingException {
        ReflectionTestUtils.setField(emailNotificationService, "adminEmail", "");

        emailNotificationService.notifyAdminsOnKycSubmission(user);

        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnKycStatusChange_Verified() throws MessagingException {
        when(templateEngine.process(eq("email-kyc-approved"), any(Context.class)))
                .thenReturn("<html>KYC Approved</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.notifyUserOnKycStatusChange(user, KycStatus.VERIFIED, null);

        verify(templateEngine, times(1)).process(eq("email-kyc-approved"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnKycStatusChange_Rejected() throws MessagingException {
        when(templateEngine.process(eq("email-kyc-rejected"), any(Context.class)))
                .thenReturn("<html>KYC Rejected</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.notifyUserOnKycStatusChange(user, KycStatus.REJECTED, "ID expired");

        verify(templateEngine, times(1)).process(eq("email-kyc-rejected"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnKycStatusChange_Submitted() throws MessagingException {
        when(templateEngine.process(eq("email-kyc-submitted"), any(Context.class)))
                .thenReturn("<html>KYC Submitted</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailNotificationService.notifyUserOnKycStatusChange(user, KycStatus.SUBMITTED, null);

        verify(templateEngine, times(1)).process(eq("email-kyc-submitted"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnKycStatusChange_Default() throws MessagingException {
        when(templateEngine.process(eq("email-kyc-update"), any(Context.class))).thenReturn("<html>KYC Update</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Using a status not explicitly handled in the switch to hit default
        emailNotificationService.notifyUserOnKycStatusChange(user, KycStatus.PENDING, null);

        verify(templateEngine, times(1)).process(eq("email-kyc-update"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnMintStatusChange_Pending() throws MessagingException {
        when(templateEngine.process(eq("email-mint-pending"), any(Context.class)))
                .thenReturn("<html>Mint Pending</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));
        mint.setStatus(MintStatus.PENDING);

        emailNotificationService.notifyUserOnMintStatusChange(user, mint, null);

        verify(templateEngine, times(1)).process(eq("email-mint-pending"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnMintStatusChange_Confirmed() throws MessagingException {
        when(templateEngine.process(eq("email-mint-confirmed"), any(Context.class)))
                .thenReturn("<html>Mint Confirmed</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));
        mint.setStatus(MintStatus.CONFIRMED);

        emailNotificationService.notifyUserOnMintStatusChange(user, mint, null);

        verify(templateEngine, times(1)).process(eq("email-mint-confirmed"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnMintStatusChange_Minted() throws MessagingException {
        when(templateEngine.process(eq("email-mint-minted"), any(Context.class)))
                .thenReturn("<html>Mint Minted</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));
        mint.setStatus(MintStatus.MINTED);

        emailNotificationService.notifyUserOnMintStatusChange(user, mint, null);

        verify(templateEngine, times(1)).process(eq("email-mint-minted"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnMintStatusChange_Transferred() throws MessagingException {
        when(templateEngine.process(eq("email-mint-transferred"), any(Context.class)))
                .thenReturn("<html>Mint Transferred</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));
        mint.setStatus(MintStatus.TRANSFERRED);

        emailNotificationService.notifyUserOnMintStatusChange(user, mint, null);

        verify(templateEngine, times(1)).process(eq("email-mint-transferred"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void notifyUserOnMintStatusChange_Failed() throws MessagingException {
        when(templateEngine.process(eq("email-mint-failed"), any(Context.class)))
                .thenReturn("<html>Mint Failed</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));
        mint.setStatus(MintStatus.FAILED);

        emailNotificationService.notifyUserOnMintStatusChange(user, mint, null);

        verify(templateEngine, times(1)).process(eq("email-mint-failed"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
