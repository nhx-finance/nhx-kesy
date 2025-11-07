package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MintRequest(
    @NotNull
    @DecimalMin(value = "10000000", message = "Amount to mint must be at least 10,000,000 KES")
    BigDecimal amountKes,
    @NotNull UUID walletId,
    @NotNull LocalDate dateInitiated
) {}
