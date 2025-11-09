package com.javaguy.nhx.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MintResponse {
    private UUID requestId;
    private UUID transactionId;
}
