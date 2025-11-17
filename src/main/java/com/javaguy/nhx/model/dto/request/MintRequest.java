package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MintRequest {

    @NotNull(message = "Amount in KES is required")
    @DecimalMin(value = "100.00", message = "Minimum mint amount is 100 KES")
    private BigDecimal amountKes;

    @NotNull(message = "Wallet ID is required")
    private UUID walletId;
    
    @NotNull(message = "Transaction message is required")
    private String transaction_message;
}
