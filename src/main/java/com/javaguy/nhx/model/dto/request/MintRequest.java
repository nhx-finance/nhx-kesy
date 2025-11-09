package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MintRequest {

    @NotNull(message = "Amount in KES is required")
    @DecimalMin(value = "10000000.00", message = "Minimum mint amount is 10,000,000 KES")
    private BigDecimal amountKes;

    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
}
