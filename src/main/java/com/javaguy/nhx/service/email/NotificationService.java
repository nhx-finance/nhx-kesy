package com.javaguy.nhx.service.email;

import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;

public interface NotificationService {

    void notifyUserOnKycStatusChange(User user, KycStatus status, String s);
    void notifyAdminsOnKycSubmission(User user);
    void notifyUserOnMintStatusChange(User user, Mint mint, String notes);
    void sendOtpEmail(String email, String otp);
    void sendWelcomeEmail(User user);
}
