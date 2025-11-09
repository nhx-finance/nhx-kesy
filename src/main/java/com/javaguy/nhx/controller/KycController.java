package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.KycSubmissionRequest;
import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Operation(
            summary = "Initiate KYC Process",
            description = "Initiates the KYC verification process for the authenticated user. Payment integration is currently on hold."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "KYC initiation successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - not authenticated",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> initiateKyc(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("KYC initiation requested by user: {}", currentUser.getEmail());

        Map<String, String> response = kycService.initiateKyc(currentUser.getId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(
            summary = "Submit KYC Documents",
            description = "Upload KYC documents (front and back of ID/Passport/Driver's License) along with personal details for verification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "KYC documents submitted successfully and are under review",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KycSubmissionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid file type or missing required fields",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - not authenticated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failed to store documents",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KycSubmissionResponse> submitKycDocuments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestPart("data") KycSubmissionRequest data,
            @RequestPart(value = "documentFront", required = true) MultipartFile documentFront,
            @RequestPart(value = "documentBack", required = true) MultipartFile documentBack) {

        log.info("KYC submission received from user: {} with document type: {}",
                currentUser.getEmail(), data.getDocumentType());

        KycSubmissionResponse response = kycService.submitDocuments(
                currentUser.getId(),
                data,
                documentFront,
                documentBack
        );

        log.info("KYC submission successful for user: {}. KYC ID: {}",
                currentUser.getEmail(), response.getKycId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(
            summary = "Get KYC Status",
            description = "Retrieves the current KYC verification status of the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KYC status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KycStatusResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - not authenticated",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KycStatusResponse> getKycStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.debug("KYC status requested by user: {}", currentUser.getEmail());

        KycStatusResponse response = kycService.getKycStatus(currentUser.getId());

        return ResponseEntity.ok(response);
    }
}