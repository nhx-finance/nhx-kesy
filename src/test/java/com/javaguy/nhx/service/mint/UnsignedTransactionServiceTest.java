package com.javaguy.nhx.service.mint;

import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.ServiceUnavailableException;
import com.javaguy.nhx.model.dto.request.UnsignedTransactionRequest;
import com.javaguy.nhx.model.dto.response.UnsignedTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnsignedTransactionServiceTest {

        @Mock
        private RestClient restClient;
        @Mock
        private RestClient.RequestBodyUriSpec requestBodyUriSpec;
        @Mock
        private RequestBodySpec requestBodySpec;
        @Mock
        private ResponseSpec responseSpec;

        @InjectMocks
        private UnsignedTransactionService unsignedTransactionService;

        private String multisigApiUrl = "http://mock-multisig-api.com";
        private UnsignedTransactionRequest unsignedTransactionRequest;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(unsignedTransactionService, "multisigApiUrl", multisigApiUrl);

                unsignedTransactionRequest = UnsignedTransactionRequest.builder()
                                .transaction_message("test message")
                                .description("test description")
                                .hedera_account_id("1.2.3")
                                .key_list(null)
                                .threshold(1)
                                .network("testnet")
                                .start_date("2023-01-01T00:00:00Z")
                                .build();

                lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
                lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
                lenient().when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
                lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
                lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
                lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void createUnsignedTransaction_Success() {
                UUID mockTransactionId = UUID.randomUUID();
                UnsignedTransactionResponse mockResponse = new UnsignedTransactionResponse(
                                mockTransactionId.toString());
                when(responseSpec.body(UnsignedTransactionResponse.class)).thenReturn(mockResponse);

                UnsignedTransactionResponse response = unsignedTransactionService
                                .createUnsignedTransaction(unsignedTransactionRequest);

                assertNotNull(response);
                assertEquals(mockTransactionId.toString(), response.getTransactionId());
                verify(restClient, times(1)).post();
                verify(responseSpec, times(1)).body(UnsignedTransactionResponse.class);
        }

        @Test
        void createUnsignedTransaction_HttpClientError_ThrowsBadRequestException() {
                lenient().when(responseSpec.body(UnsignedTransactionResponse.class))
                                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> unsignedTransactionService.createUnsignedTransaction(unsignedTransactionRequest));

                assertTrue(exception.getMessage().contains("Client error while calling multisig API"));
        }

        @Test
        void createUnsignedTransaction_HttpServerError_ThrowsInternalServerException() {
                lenient().when(responseSpec.body(UnsignedTransactionResponse.class))
                                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Internal Server Error"));

                InternalServerException exception = assertThrows(InternalServerException.class,
                                () -> unsignedTransactionService.createUnsignedTransaction(unsignedTransactionRequest));

                assertTrue(exception.getMessage().contains("Server error while calling multisig API"));
        }

        @Test
        void createUnsignedTransaction_ResourceAccessException_ThrowsServiceUnavailableException() {
                lenient().when(responseSpec.body(UnsignedTransactionResponse.class))
                                .thenThrow(new ResourceAccessException("I/O error"));

                ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                                () -> unsignedTransactionService.createUnsignedTransaction(unsignedTransactionRequest));

                assertTrue(exception.getMessage().contains("Cannot reach multisig service"));
        }

        @Test
        void createUnsignedTransaction_GenericException_ThrowsInternalServerException() {
                lenient().when(responseSpec.body(UnsignedTransactionResponse.class))
                                .thenThrow(new RuntimeException("Unexpected error"));

                InternalServerException exception = assertThrows(InternalServerException.class,
                                () -> unsignedTransactionService.createUnsignedTransaction(unsignedTransactionRequest));

                assertTrue(exception.getMessage().contains("An unexpected error occurred with the multisig service"));
        }
}
