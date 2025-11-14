package com.javaguy.nhx.service;

import com.javaguy.nhx.model.dto.request.UnsignedTransactionRequest;
import com.javaguy.nhx.model.dto.response.UnsignedTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnsignedTransactionService {

    @Value("${multisig.api.url}")
    private String multisigApiUrl;

    private final RestClient restClient;

    public UnsignedTransactionResponse createUnsignedTransaction(UnsignedTransactionRequest request) {
        String url = multisigApiUrl + "/api/transactions";
        log.info("Sending request to multisig API: {}", url);
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(UnsignedTransactionResponse.class);
    }
}
