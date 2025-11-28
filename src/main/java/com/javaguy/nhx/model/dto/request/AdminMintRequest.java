package com.javaguy.nhx.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminMintRequest(
        @JsonProperty("amount") String amount) {
}
