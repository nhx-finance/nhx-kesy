package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MintAdminResponse {
    private UUID requestId;
    private UUID userId;
    private String userEmail;
    private String walletAddress;
    private BigDecimal amountKes;
    private MintStatus status;
    private LocalDate dateInitiated;
    private LocalDate restrictionEndDate;
    private String paymentReference;
    private String treasuryTransactionId;
    private LocalDateTime createdAt;
}
