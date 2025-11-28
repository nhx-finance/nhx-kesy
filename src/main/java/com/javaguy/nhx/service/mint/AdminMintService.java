package com.javaguy.nhx.service.mint;

import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.ServiceUnavailableException;
import com.javaguy.nhx.model.dto.request.AdminMintRequest;
import com.javaguy.nhx.model.dto.request.AdminTransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMintService {

    private final RestClient restClient;
    @Value("${sdk.url}")
    private String SDK_URL;
    @Value("${sdk.sdk-api-key}")
    private String SDK_API_KEY;

    public String transfer(AdminTransferRequest request) {
        String url = SDK_URL + "/api/token/transfer";
        log.info("preparing to make transfer request amount: {}, to: {}", request.amount(), request.accountId());
        
        try {
            String response = restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", SDK_URL)
                    .header("Authorization", "Bearer " + SDK_API_KEY)
                    .retrieve()
                    .body(String.class);
            log.info("transfer response: {}", response);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Client error from sdk API (4xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BadRequestException("Client error while calling sdk API: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.error("Server error from sdk API (5xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new InternalServerException("Server error while calling sdk API", e);

        } catch (ResourceAccessException e) {
            log.error("Network error while calling sdk API at {}: {}", url, e.getMessage(), e);
            throw new ServiceUnavailableException("Cannot reach sdk service. Please try again later.", e);

        } catch (Exception e) {
            log.error("Unexpected error while calling sdk API: {}", e.getMessage(), e);
            throw new InternalServerException("An unexpected error occurred with the sdk service. Please try again later.", e);
        }
    }

    //mint service
    public String mint(AdminMintRequest request) {
        String url = SDK_URL + "/api/mint";
        log.info("Mint request: {}", request);

        try {
            String response = restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", SDK_URL)
                    .header("Authorization", "Bearer " + SDK_API_KEY)
                    .retrieve()
                    .body(String.class);
            log.info("mint response: {}", response);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Client error from sdk API (4xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BadRequestException("Client error while calling sdk API: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.error("Server error from sdk API (5xx) at {}: Status={}, Body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new InternalServerException("Server error while calling sdk API", e);

        } catch (ResourceAccessException e) {
            log.error("Network error while calling sdk API at {}: {}", url, e.getMessage(), e);
            throw new ServiceUnavailableException("Cannot reach sdk service. Please try again later.", e);

        } catch (Exception e) {
            log.error("Unexpected error while calling sdk API: {}", e.getMessage(), e);
            throw new InternalServerException("An unexpected error occurred with the sdk service. Please try again later.", e);
        }
    }
}
