package com.javaguy.nhx.model.dto.response;

import com.javaguy.nhx.model.enums.MintStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserDashboardResponse {
    private BigDecimal amountMinted;
    private BigDecimal pendingMints;
    private List<TransactionSummary> transactions;
    
    @Data
    @Builder
    public static class TransactionSummary {
        private LocalDate date;
        private BigDecimal amount;
        private MintStatus status;
    }
}
