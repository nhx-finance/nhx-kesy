package com.javaguy.nhx.service.kyc;

import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.KycSubmissionRequest;
import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.model.entity.KycDocument;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.repository.KycDocumentRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.service.email.NotificationService;
import com.javaguy.nhx.service.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final UserRepository userRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final DocumentStorageService storageService;
    private final NotificationService notificationService;

    @Transactional
    public Map<String, String> initiateKyc(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Implement payment logic when ready
        user.setKycStatus(KycStatus.INITIATED);
        userRepository.save(user);

        log.info("KYC initiated for user {}", userId);

        return Map.of(
                "kycId", user.getId().toString(),
                "status", user.getKycStatus().name(),
                "message", "KYC initiated. Proceed with document submission."
        );
    }

    @Transactional
    public KycSubmissionResponse submitDocuments(
            UUID userId,
            KycSubmissionRequest request,
            MultipartFile documentFront,
            MultipartFile documentBack) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate user can submit KYC
        if (user.getKycStatus() == KycStatus.VERIFIED) {
            throw new ConflictException("KYC already verified");
        }

        // Validate required documents
        if (documentFront == null || documentFront.isEmpty()) {
            throw new BadRequestException("Document front is required");
        }

        if (documentBack == null || documentBack.isEmpty()) {
            throw new BadRequestException("Document back is required");
        }

        validateDocumentType(documentFront);
        validateDocumentType(documentBack);

        try {
            String frontPath = storageService.store("kyc/" + userId, documentFront);
            String backPath = storageService.store("kyc/" + userId, documentBack);

            KycDocument kycDoc = KycDocument.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .dob(request.getDob())
                    .documentType(request.getDocumentType())
                    .documentNumber(request.getDocumentNumber())
                    .documentFrontPath(frontPath)
                    .documentBackPath(backPath)
                    .build();

            kycDocumentRepository.save(kycDoc);

            user.setKycStatus(KycStatus.SUBMITTED);
            userRepository.save(user);

            log.info("KYC documents submitted by user {}. Front: {}, Back: {}",
                    userId, frontPath, backPath);

            notificationService.notifyAdminsOnKycSubmission(user);

            return KycSubmissionResponse.builder()
                    .kycId(kycDoc.getId().toString())
                    .status(user.getKycStatus().name())
                    .message("KYC documents submitted successfully. Your submission is under review.")
                    .build();

        } catch (Exception e) {
            log.error("Failed to store KYC documents for user {}: {}", userId, e.getMessage());
            throw new InternalServerException("Failed to upload KYC documents. Please try again.", e);
        }
    }

    @Transactional(readOnly = true)
    public KycStatusResponse getKycStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> documents = List.of();
        if (user.getKycStatus() == KycStatus.SUBMITTED ||
                user.getKycStatus() == KycStatus.VERIFIED) {

            List<KycDocument> kycDocs = kycDocumentRepository.findByUserId(userId);
            if (!kycDocs.isEmpty()) {
                documents = List.of("documentFront", "documentBack");
            }
        }

        return KycStatusResponse.builder()
                .status(user.getKycStatus())
                .documents(documents)
                .build();
    }

    private void validateDocumentType(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null || filename == null) {
            throw new BadRequestException("Invalid file");
        }

        boolean isValidType = contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("application/pdf");

        boolean hasValidExtension = filename.toLowerCase().endsWith(".jpg") ||
                filename.toLowerCase().endsWith(".jpeg") ||
                filename.toLowerCase().endsWith(".png") ||
                filename.toLowerCase().endsWith(".pdf");

        if (!isValidType || !hasValidExtension) {
            throw new BadRequestException(
                    "Invalid document type. Accepted formats: JPG, JPEG, PNG, PDF"
            );
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BadRequestException(
                    "File size exceeds maximum allowed size of 10MB"
            );
        }
    }
}