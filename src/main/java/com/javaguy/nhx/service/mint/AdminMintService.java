package com.javaguy.nhx.service.mint;

import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.ServiceUnavailableException;
import com.javaguy.nhx.model.dto.request.AdminMintRequest;
import com.javaguy.nhx.model.dto.request.AdminTransferRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMintService {

    private final RestClient restClient;
    @Value("${sdk.url}")
    private String SDK_URL;
    @Value("${sdk.sdk-api-key}")
    private String SDK_API_KEY;

    @Retry(name = "sdkApi")
    @CircuitBreaker(name = "sdkApi", fallbackMethod = "transferFallback")
    public String transfer(AdminTransferRequest request) {
        String url = SDK_URL + "/api/token/transfer";
        String requestId = UUID.randomUUID().toString();

        try {
            String response = restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", SDK_URL)
                    .header("Authorization", "Bearer " + SDK_API_KEY)
                    .header("X-Request-ID", requestId)
                    .header("Idempotency-Key", requestId)
                    .retrieve()
                    .body(String.class);
            return response;
        } catch (HttpClientErrorException e) {
            log.warn("SDK client error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new BadRequestException("Client error while calling sdk API: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.warn("SDK server error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new InternalServerException("Server error while calling sdk API", e);

        } catch (ResourceAccessException e) {
            log.warn("SDK network error: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot reach sdk service. Please try again later.", e);

        } catch (Exception e) {
            log.error("SDK unexpected error: {}", e.getMessage(), e);
            throw new InternalServerException(
                    "An unexpected error occurred with the sdk service. Please try again later.", e);
        }
    }

    public String transferFallback(AdminTransferRequest request, Exception ex) {
        log.warn("SDK circuit breaker open for transfer");
        throw new ServiceUnavailableException("SDK service temporarily unavailable. Please retry later.", ex);
    }

    @Retry(name = "sdkApi")
    @CircuitBreaker(name = "sdkApi", fallbackMethod = "mintFallback")
    public String mint(AdminMintRequest request) {
        String url = SDK_URL + "/api/mint";
        String requestId = UUID.randomUUID().toString();

        try {
            String response = restClient.post()
                    .uri(url)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Origin", SDK_URL)
                    .header("Authorization", "Bearer " + SDK_API_KEY)
                    .header("X-Request-ID", requestId)
                    .header("Idempotency-Key", requestId)
                    .retrieve()
                    .body(String.class);
            return response;
        } catch (HttpClientErrorException e) {
            log.warn("SDK client error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new BadRequestException("Client error while calling sdk API: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            log.warn("SDK server error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new InternalServerException("Server error while calling sdk API", e);

        } catch (ResourceAccessException e) {
            log.warn("SDK network error: {}", e.getMessage());
            throw new ServiceUnavailableException("Cannot reach sdk service. Please try again later.", e);

        } catch (Exception e) {
            log.error("SDK unexpected error: {}", e.getMessage(), e);
            throw new InternalServerException(
                    "An unexpected error occurred with the sdk service. Please try again later.", e);
        }
    }

    public String mintFallback(AdminMintRequest request, Exception ex) {
        log.warn("SDK circuit breaker open for mint");
        throw new ServiceUnavailableException("SDK service temporarily unavailable. Please retry later.", ex);
    }
}
