package com.javaguy.nhx.service.admin;

import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.UpdateKycStatusRequest;
import com.javaguy.nhx.model.dto.request.UpdateMintStatusRequest;
import com.javaguy.nhx.model.dto.response.KycSubmissionAdminResponse;
import com.javaguy.nhx.model.dto.response.MintAdminResponse;
import com.javaguy.nhx.model.entity.KycDocument;
import com.javaguy.nhx.model.entity.Mint;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.repository.KycDocumentRepository;
import com.javaguy.nhx.repository.MintRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.service.email.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final MintRepository mintRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<KycSubmissionAdminResponse> getAllKycSubmissions(
            KycStatus status, 
            int page, 
            int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<KycDocument> kycDocs;
        
        if (status != null) {
            kycDocs = kycDocumentRepository.findByUser_KycStatus(status, pageable);
        } else {
            kycDocs = kycDocumentRepository.findAll(pageable);
        }
        
        return kycDocs.map(doc -> KycSubmissionAdminResponse.builder()
                .kycId(doc.getId())
                .userId(doc.getUser().getId())
                .userEmail(doc.getUser().getEmail())
                .fullName(doc.getFullName())
                .dob(doc.getDob())
                .documentType(doc.getDocumentType())
                .documentNumber(doc.getDocumentNumber())
                .documentFrontPath(doc.getDocumentFrontPath())
                .documentBackPath(doc.getDocumentBackPath())
                .status(doc.getUser().getKycStatus())
                .submittedAt(doc.getSubmittedAt())
                .build());
    }

    @Transactional
    public void updateKycStatus(UUID kycId, UpdateKycStatusRequest request) {
        KycDocument kycDoc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        
        User user = kycDoc.getUser();
        KycStatus oldStatus = user.getKycStatus();
        
        user.setKycStatus(request.getStatus());
        userRepository.save(user);
        
        log.info("Admin updated KYC status from {} to {} for user {}", 
                oldStatus, request.getStatus(), user.getId());
        
         //Notify user
        notificationService.notifyUserOnKycStatusChange(
                user,
                request.getStatus(),
                request.getRejectionReason() != null ? request.getRejectionReason() : request.getReviewerNotes()
        );
    }

    @Transactional(readOnly = true)
    public Page<MintAdminResponse> getAllMints(MintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateInitiated").descending());
        Page<Mint> mints;
        
        if (status != null) {
            mints = mintRepository.findByStatus(status, pageable);
        } else {
            mints = mintRepository.findAll(pageable);
        }
        
        return mints.map(mint -> MintAdminResponse.builder()
                .requestId(mint.getId())
                .userId(mint.getUser().getId())
                .userEmail(mint.getUser().getEmail())
                .walletAddress(mint.getWallet().getWalletAddress())
                .amountKes(mint.getAmountKes())
                .status(mint.getStatus())
                .dateInitiated(mint.getDateInitiated())
                .restrictionEndDate(mint.getRestrictionEndDate())
                .paymentReference(mint.getPaymentReference())
                .createdAt(mint.getCreatedAt())
                .build());
    }

    @Transactional
    public void updateMintStatus(UUID mintId, UpdateMintStatusRequest request) {
        Mint mint = mintRepository.findById(mintId)
                .orElseThrow(() -> new ResourceNotFoundException("Mint request not found"));
        
        MintStatus oldStatus = mint.getStatus();
        mint.setStatus(request.getStatus());
        
        mintRepository.save(mint);
        
        log.info("Admin updated mint status from {} to {} for mint {}", 
                oldStatus, request.getStatus(), mintId);
        
        notificationService.notifyUserOnMintStatusChange(
                mint.getUser(),
                mint,
                request.getNotes()
        );
    }
}
