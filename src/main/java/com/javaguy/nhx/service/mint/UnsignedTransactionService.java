package com.javaguy.nhx.service.mint;

import com.javaguy.nhx.model.dto.request.UnsignedTransactionRequest;
import com.javaguy.nhx.model.dto.response.UnsignedTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.InternalServerException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnsignedTransactionService {

    @Value("${multisig.api.url}")
    private String multisigApiUrl;

    private final RestClient restClient;

    public UnsignedTransactionResponse createUnsignedTransaction(UnsignedTransactionRequest request) {
        String url = multisigApiUrl + "/api/transactions";

        log.info("Preparing to send createUnsignedTransaction request to {}", url);
        log.debug("Request payload: {}", request);

        try {
            UnsignedTransactionResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", multisigApiUrl)
                    .body(request)
                    .retrieve()
                    .body(UnsignedTransactionResponse.class);

            log.info("Successfully received unsigned transaction response");
            log.debug("Response payload: {}", response);

            return response;

        } catch (HttpClientErrorException e) {
            log.error("Client error from multisig API (4xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BadRequestException("Client error while calling multisig API: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.error("Server error from multisig API (5xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new InternalServerException("Server error while calling multisig API", e);

        } catch (ResourceAccessException e) {
            log.error("Network error while calling multisig API at {}: {}", url, e.getMessage(), e);
            throw new InternalServerException("Network error while calling multisig API, please try again", e);

        } catch (Exception e) {
            log.error("Unexpected error while calling multisig API: {}", e.getMessage(), e);
            throw new InternalServerException("Unexpected error while creating unsigned transaction, please try again", e);
        }
    }
}
