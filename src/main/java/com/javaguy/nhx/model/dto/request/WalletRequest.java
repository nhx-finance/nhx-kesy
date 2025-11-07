package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WalletRequest(
    @NotBlank String walletAddress,
    @NotBlank String network
) {}
