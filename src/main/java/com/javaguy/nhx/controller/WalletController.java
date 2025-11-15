package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.WalletRequest;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.auth.UserService;
import jakarta.validation.Valid;
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
import com.javaguy.nhx.model.dto.response.WalletListResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final UserService userService;

    @Operation(summary = "Add whitelisted wallet", description = "Allows an authenticated and KYC-verified user to add a whitelisted wallet address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet whitelisted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or wallet already whitelisted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated or KYC not verified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addWhitelistedWallet(
            @Valid @RequestBody WalletRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UUID walletId = userService.addWhitelistedWallet(currentUser.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Wallet whitelisted", "walletId", walletId));
    }

    @Operation(summary = "Get whitelisted wallets", description = "Retrieves all whitelisted wallets for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of whitelisted wallets",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WalletListResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletListResponse> getWhitelistedWallets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Wallet> wallets = userService.getWhitelistedWallets(currentUser.getId());
        List<WalletListResponse.WalletSummary> walletSummaries = wallets.stream()
                .map(wallet -> WalletListResponse.WalletSummary.builder()
                        .walletId(wallet.getId())
                        .address(wallet.getWalletAddress())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(WalletListResponse.builder().wallets(walletSummaries).build());
    }
}
