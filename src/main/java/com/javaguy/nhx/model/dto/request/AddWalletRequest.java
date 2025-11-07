package com.javaguy.nhx.model.dto.request;

import com.javaguy.nhx.model.enums.Network;
import com.javaguy.nhx.validation.HederaAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddWalletRequest(
        @NotBlank @HederaAccount String walletAddress,
        @NotNull Network network,
        @NotBlank String nickname
) {}
