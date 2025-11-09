package com.javaguy.nhx.service;

import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${admin.notification.email:}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.name}")
    private String appName;

    @Value("${app.support-email}")
    private String supportEmail;

    @Override
    public void sendOtpEmail(String email, String otp) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otp);
        variables.put("expiryMinutes", 10);

        sendHtmlEmail(
                email,
                "Your " + appName + " Verification Code",
                "email-otp",
                variables,
                "OTP"
        );
    }

    @Override
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "there");
        variables.put("dashboardUrl", appBaseUrl + "/dashboard");

        sendHtmlEmail(
                user.getEmail(),
                "Welcome to " + appName,
                "email-welcome",
                variables,
                "Welcome"
        );
    }

    @Override
    public void notifyAdminsOnKycSubmission(User user) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("[Notification] No admin recipient configured. Skipping admin notification.");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("userId", user.getId().toString());
        variables.put("userEmail", user.getEmail());
        variables.put("userName", (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : ""));
        variables.put("submittedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        variables.put("adminPortalUrl", appBaseUrl + "/admin/kyc");

        sendHtmlEmail(
                adminEmail.trim(),
                "KYC Submission Received: " + user.getEmail(),
                "email-admin-kyc-submission",
                variables,
                "KYC Submission Admin"
        );
    }

    @Override
    public void notifyUserOnKycStatusChange(User user, KycStatus status, String notes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "there");
        variables.put("status", status.name());
        variables.put("notes", notes);
        variables.put("dashboardUrl", appBaseUrl + "/dashboard");

        String template = switch (status) {
            case VERIFIED -> "email-kyc-approved";
            case REJECTED -> "email-kyc-rejected";
            case SUBMITTED -> "email-kyc-submitted";
            default -> "email-kyc-update";
        };

        String subject = switch (status) {
            case VERIFIED -> "KYC Verification Approved ✓";
            case REJECTED -> "KYC Verification - Action Required";
            case SUBMITTED -> "KYC Documents Received";
            default -> "KYC Status Update";
        };

        sendHtmlEmail(user.getEmail(), subject, template, variables, "KYC Status Change");
    }

    @Override
    public void notifyUserOnMintStatusChange(User user, Mint mint, String notes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "there");
        variables.put("amount", mint.getAmountKes().toString());
        variables.put("walletAddress", mint.getWallet().getWalletAddress());
        variables.put("requestId", mint.getId().toString());
        variables.put("notes", notes);
        variables.put("mintsUrl", appBaseUrl + "/dashboard/mints");

        String template = switch (mint.getStatus()) {
            case PENDING -> "email-mint-pending";
            case CONFIRMED -> "email-mint-confirmed";
            case MINTED -> "email-mint-minted";
            case TRANSFERRED -> "email-mint-transferred";
            case FAILED -> "email-mint-failed";
        };

        String subject = switch (mint.getStatus()) {
            case PENDING -> "Mint Request Received";
            case CONFIRMED -> "Mint Request Confirmed - Processing";
            case MINTED -> "KESY Tokens Minted to Treasury";
            case TRANSFERRED -> "KESY Tokens Transferred to Your Wallet ✓";
            case FAILED -> "Mint Request Failed - Action Required";
        };

        sendHtmlEmail(user.getEmail(), subject, template, variables, "Mint Status Change");
    }

    private void sendHtmlEmail(String to, String subject, String templateName,
                               Map<String, Object> variables, String type) {
        try {
            variables.put("appName", appName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", java.time.Year.now().getValue());

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent {} HTML email to: {}", type, to);
        } catch (MessagingException e) {
            log.error("Failed to send {} email to {}: {}", type, to, e.getMessage(), e);
        }
    }
}