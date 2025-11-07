package com.javaguy.nhx.controller;

import com.javaguy.nhx.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/kyc")
@RequiredArgsConstructor
@Tag(name = "Admin KYC", description = "Admin operations for KYC management")
public class AdminKycController {

    private final KycService kycService;

    @Operation(summary = "Mark KYC as completed (VERIFIED) for a user")
    @PostMapping("/{userId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markKycComplete(@PathVariable("userId") UUID userId) {
        kycService.markKycStatus(userId, com.javaguy.nhx.model.enums.KycStatus.VERIFIED);
        return ResponseEntity.ok(Map.of(
                "message", "KYC marked as VERIFIED",
                "userId", userId
        ));
    }
}
