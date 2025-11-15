package com.javaguy.nhx.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnsignedTransactionRequest {
    private String transaction_message;
    private String description;
    private String hedera_account_id;
    private List<String> key_list;
    private Integer threshold;
    private String network;
    private String start_date;
}
