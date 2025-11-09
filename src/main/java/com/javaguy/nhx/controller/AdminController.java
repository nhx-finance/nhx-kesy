package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.UpdateKycStatusRequest;
import com.javaguy.nhx.model.dto.request.UpdateMintStatusRequest;
import com.javaguy.nhx.model.dto.response.KycSubmissionAdminResponse;
import com.javaguy.nhx.model.dto.response.MintAdminResponse;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ============ KYC ENDPOINTS ============
    
    @GetMapping("/kyc")
    public ResponseEntity<Page<KycSubmissionAdminResponse>> getAllKycSubmissions(
            @RequestParam(required = false) KycStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<KycSubmissionAdminResponse> submissions = adminService.getAllKycSubmissions(status, page, size);
        return ResponseEntity.ok(submissions);
    }

    @PatchMapping("/kyc/{kycId}/status")
    public ResponseEntity<Void> updateKycStatus(
            @PathVariable UUID kycId,
            @RequestBody UpdateKycStatusRequest request) {
        
        adminService.updateKycStatus(kycId, request);
        return ResponseEntity.ok().build();
    }

    // ============ MINT ENDPOINTS ============
    
    @GetMapping("/mints")
    public ResponseEntity<Page<MintAdminResponse>> getAllMints(
            @RequestParam(required = false) MintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<MintAdminResponse> mints = adminService.getAllMints(status, page, size);
        return ResponseEntity.ok(mints);
    }

    @PatchMapping("/mints/{mintId}/status")
    public ResponseEntity<Void> updateMintStatus(
            @PathVariable UUID mintId,
            @RequestBody UpdateMintStatusRequest request) {
        
        adminService.updateMintStatus(mintId, request);
        return ResponseEntity.ok().build();
    }
}
