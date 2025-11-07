package com.javaguy.nhx.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WalletListResponse {
    private List<WalletSummary> wallets;

    @Data
    @Builder
    public static class WalletSummary {
        private UUID walletId;
        private String address;
    }
}
