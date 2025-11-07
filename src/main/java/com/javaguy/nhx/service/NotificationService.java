package com.javaguy.nhx.service;

import com.javaguy.nhx.model.entity.User;

public interface NotificationService {
    void notifyAdminsOnKycSubmission(User user);

}
