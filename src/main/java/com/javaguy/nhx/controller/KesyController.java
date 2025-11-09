package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.MintRequest;
import com.javaguy.nhx.model.dto.response.MintResponse;
import com.javaguy.nhx.model.dto.response.MintStatusResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.MintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/kesy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Token Minting", description = "APIs for managing token minting requests")
public class KesyController {

    private final MintService mintService;

    @Operation(summary = "Request Token Mint", description = "Submits a request to mint KESY tokens. Requires KYC VERIFIED status and a minimum amount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mint request created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MintResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid amount, wallet, or KYC status",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authenticated or KYC not verified",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not Found - User or Wallet not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/mint")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MintResponse> requestMint(
            @Valid @RequestBody MintRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MintResponse response = mintService.requestMint(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Check Mint Status", description = "Retrieves the status of a specific token mint request by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mint status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MintStatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authenticated",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not Found - Mint request not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/mint/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MintStatusResponse> getMintStatus(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MintStatusResponse response = mintService.getMintStatus(currentUser.getId(), requestId);
        return ResponseEntity.ok(response);
    }
}
