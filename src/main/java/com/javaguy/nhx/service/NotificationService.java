package com.javaguy.nhx.service;

import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public void notifyAdminsOnKycSubmission(User user) {
        log.info("Notifying admins of KYC submission for user: {}", user.getEmail());
        // TODO: Implement email/notification to admins
    }

    public void notifyUserOnKycStatusChange(User user, KycStatus status, String notes) {
        log.info("Notifying user {} of KYC status change to: {}", user.getEmail(), status);
        // TODO: Implement email notification to user
    }

    public void notifyUserOnMintStatusChange(User user, Mint mint, String notes) {
        log.info("Notifying user {} of mint status change to: {}", user.getEmail(), mint.getStatus());
        // TODO: Implement email notification to user
    }
}
