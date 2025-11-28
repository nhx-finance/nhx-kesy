package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.exception.custom.KycNotVerifiedException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.MintRequest;
import com.javaguy.nhx.model.dto.response.MintResponse;
import com.javaguy.nhx.model.dto.response.MintStatusResponse;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.WithUserPrincipal;
import com.javaguy.nhx.service.mint.MintRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KesyController.class)
class KesyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private MintRequestService mintRequestService;
        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;
        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;
        @Autowired
        private ObjectMapper objectMapper;

        private MintRequest mintRequest;
        private MintResponse mintResponse;
        private MintStatusResponse mintStatusResponse;
        private UUID requestId;

        @BeforeEach
        void setUp() {
                requestId = UUID.randomUUID();
                UUID walletId = UUID.randomUUID();

                mintRequest = new MintRequest();
                mintRequest.setAmountKes(BigDecimal.valueOf(1000));
                mintRequest.setWalletId(walletId);
                mintRequest.setTransaction_message("test message");
                mintResponse = MintResponse.builder().requestId(requestId).transactionId(UUID.randomUUID()).build();
                mintStatusResponse = MintStatusResponse.builder()
                                .requestId(requestId)
                                .status(MintStatus.PENDING)
                                .tokensMinted(BigDecimal.valueOf(1000))
                                .dateInitiated(LocalDateTime.now())
                                .build();
        }

        // Test for requestMint endpoint
        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void requestMint_AuthenticatedUser_ReturnsCreated() throws Exception {
                when(mintRequestService.requestMint(any(UUID.class), any(MintRequest.class)))
                                .thenReturn(mintResponse);

                mockMvc.perform(post("/api/kesy/mint")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mintRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.requestId").value(requestId.toString()));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void requestMint_InvalidInput_ReturnsBadRequest() throws Exception {
                MintRequest invalidMintRequest = new MintRequest(); // Invalid input (null values)

                mockMvc.perform(post("/api/kesy/mint")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidMintRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void requestMint_KycNotVerified_ReturnsBadRequest() throws Exception {
                when(mintRequestService.requestMint(any(UUID.class), any(MintRequest.class)))
                                .thenThrow(new KycNotVerifiedException("KYC not verified"));

                mockMvc.perform(post("/api/kesy/mint")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mintRequest)))
                                .andExpect(status().isBadRequest()); // Assuming KycNotVerifiedException maps to 400
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void requestMint_ResourceNotFound_ReturnsNotFound() throws Exception {
                when(mintRequestService.requestMint(any(UUID.class), any(MintRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Wallet not found"));

                mockMvc.perform(post("/api/kesy/mint")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mintRequest)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void requestMint_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(post("/api/kesy/mint")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mintRequest)))
                                .andExpect(status().isUnauthorized());
        }

        // Test for getMintStatus endpoint
        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getMintStatus_AuthenticatedUser_ReturnsOk() throws Exception {
                when(mintRequestService.getMintStatus(any(UUID.class), eq(requestId)))
                                .thenReturn(mintStatusResponse);

                mockMvc.perform(get("/api/kesy/mint/{requestId}", requestId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                                .andExpect(jsonPath("$.status").value(MintStatus.PENDING.name()));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getMintStatus_ResourceNotFound_ReturnsNotFound() throws Exception {
                when(mintRequestService.getMintStatus(any(UUID.class), eq(requestId)))
                                .thenThrow(new ResourceNotFoundException("Mint request not found"));

                mockMvc.perform(get("/api/kesy/mint/{requestId}", requestId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getMintStatus_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/api/kesy/mint/{requestId}", requestId))
                                .andExpect(status().isUnauthorized());
        }
}
