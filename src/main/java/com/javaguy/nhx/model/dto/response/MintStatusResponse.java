package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MintStatusResponse {
    private UUID requestId;
    private MintStatus status;
    private BigDecimal tokensMinted;
    private LocalDateTime dateInitiated;
    private LocalDateTime dateCompleted;
}
