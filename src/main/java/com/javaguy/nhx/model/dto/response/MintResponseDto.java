package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MintResponseDto {
    private UUID id;
    private BigDecimal amountKes;
    private MintStatus status;
    private LocalDate dateInitiated;
    private String treasuryTransactionId;
    private LocalDateTime createdAt;
    private String walletAddress;
}
