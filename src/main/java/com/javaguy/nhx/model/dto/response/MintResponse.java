package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class MintResponse {
    private UUID requestId;
    private MintStatus status;
    private BigDecimal tokensMinted;
    private Map<String, String> depositDetails;
    private LocalDate restrictionEndDate;
}
