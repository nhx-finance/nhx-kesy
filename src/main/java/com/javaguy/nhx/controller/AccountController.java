package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing user account details and KYC status.")
public class AccountController {

    private final KycService kycService;

    @Operation(summary = "Get KYC status", description = "Retrieves the current KYC status and submitted documents for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KYC status retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/kyc")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KycStatusResponse> getKycStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KycStatusResponse response = kycService.getKycStatus(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
