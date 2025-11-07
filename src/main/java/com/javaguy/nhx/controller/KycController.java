package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC", description = "APIs for Know Your Customer (KYC) process")
public class KycController {

    private final KycService kycService;

    @Operation(summary = "Initiate KYC", description = "Initiates the KYC process (no payment step)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "KYC initiation successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> initiateKyc(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, String> response = kycService.initiateKyc(currentUser.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
/*
    @Operation(summary = "Submit KYC documents", description = "Upload ID document and proof of address for manual review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "KYC documents submitted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Failed to store documents",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KycSubmissionResponse> submitKycDocuments(
            @RequestParam("idDocument") MultipartFile idDocument,
            @RequestParam("proofOfAddress") MultipartFile proofOfAddress,
            @RequestParam String sourceOfFunds,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KycSubmissionResponse response = kycService.submitDocuments(currentUser.getId(), idDocument, sourceOfFunds);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
*/
    @Operation(summary = "Get KYC status", description = "Retrieves the current KYC status of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KYC status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KycStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/kyc")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.javaguy.nhx.model.dto.response.KycStatusResponse> getKycStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KycStatusResponse response = kycService.getKycStatus(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
