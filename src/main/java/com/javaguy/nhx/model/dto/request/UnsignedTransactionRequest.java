package com.javaguy.nhx.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnsignedTransactionRequest {
    private String payload;
    private String description;
    private String accountId;
    private List<String> keyList;
    private Integer threshold;
}
