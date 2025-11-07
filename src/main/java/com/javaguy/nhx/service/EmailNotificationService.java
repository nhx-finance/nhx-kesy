package com.javaguy.nhx.service;

import com.javaguy.nhx.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${admin.notification.email:}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void notifyAdminsOnKycSubmission(User user) {
        String subject = "KYC Submission Received: " + user.getEmail();
        String body = "A user submitted KYC documents for manual review.\n\n"
                + "User ID: " + user.getId() + "\n"
                + "Email: " + user.getEmail() + "\n"
                + "Submitted At: " + OffsetDateTime.now() + "\n\n"
                + "Please log into the admin console to review and update the KYC status.";

        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("[Notification] No admin recipient configured (admin.notification.email). Logging message instead. subject='{}' body='{}'", subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(adminEmail.trim());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent KYC submission notification to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send admin KYC notification email: {}", e.getMessage(), e);
        }
    }
}
