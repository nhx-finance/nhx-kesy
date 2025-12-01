package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.AdminMintRequest;
import com.javaguy.nhx.model.dto.request.AdminTransferRequest;
import com.javaguy.nhx.model.dto.request.UpdateKycStatusRequest;
import com.javaguy.nhx.model.dto.request.UpdateMintStatusRequest;
import com.javaguy.nhx.model.dto.response.KycSubmissionAdminResponse;
import com.javaguy.nhx.model.dto.response.MintAdminResponse;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.service.admin.AdminService;
import com.javaguy.nhx.service.mint.AdminMintService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Controller", description = "Admin Endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AdminMintService adminMintService;

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

    // ============ MINT TOKEN ENDPOINTS ============

    @PostMapping("/mint")
    public ResponseEntity<String> mint(@RequestBody AdminMintRequest request) {
        log.info("Admin initiating mint request");
        String response = adminMintService.mint(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody AdminTransferRequest request) {
        log.info("Admin initiating transfer request for account: {}", request.targetAccountId());
        String response = adminMintService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
