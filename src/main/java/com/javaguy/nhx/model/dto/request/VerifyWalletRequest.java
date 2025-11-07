package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyWalletRequest(
        @NotBlank String signature,
        @NotBlank String publicKey
) {}
