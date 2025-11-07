package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final UserRepository userRepository;
    private final DocumentStorageService storageService;
    private final NotificationService notificationService;

    @Transactional
    public Map<String, String> initiateKyc(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //TODO: handle payment
        user.setKycStatus(KycStatus.INITIATED);
        userRepository.save(user);
        return Map.of(
                "kycId", user.getId().toString(),
                "status", user.getKycStatus().name()
        );
    }

    @Transactional
    public KycSubmissionResponse submitDocuments(UUID userId, MultipartFile idDocument, String sourceOfFunds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String idDocPath;
        try {
            idDocPath = storageService.store("kyc/" + userId + "/id", idDocument);
        } catch (IOException e) {
            log.error("Failed to store KYC documents for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to store KYC documents", e);
        }

        user.setKycStatus(KycStatus.SUBMITTED);
        userRepository.save(user);
        log.info("KYC documents submitted by user {}. idDoc={}", userId, idDocPath);

        // Notify admins to review
        notificationService.notifyAdminsOnKycSubmission(user);

        return KycSubmissionResponse.builder()
                .kycId(user.getId().toString())
                .status(user.getKycStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public KycStatusResponse getKycStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return KycStatusResponse.builder()
                .status(user.getKycStatus())
                .documents(user.getKycStatus() == KycStatus.SUBMITTED || user.getKycStatus() == KycStatus.VERIFIED
                        ? List.of("idDocument","proofOfAddress") : List.of())
                .build();
    }

    @Transactional
    public void markKycStatus(UUID userId, KycStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setKycStatus(status);
        userRepository.save(user);
        log.info("Admin set KYC status {} for user {}", status, userId);
    }
}
