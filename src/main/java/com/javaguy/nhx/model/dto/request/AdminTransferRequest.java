package com.javaguy.nhx.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminTransferRequest(
                @JsonProperty("amount") String amount,
                @JsonProperty("targetAccountId") String targetAccountId) {
}
