package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.model.dto.request.WalletRequest;
import com.javaguy.nhx.model.entity.Wallet;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.WithUserPrincipal;
import com.javaguy.nhx.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup not needed with @WithUserPrincipal annotation
    }

    @Test
    @WithUserPrincipal(roles = "ROLE_INSTITUTIONAL_USER")
    void addWhitelistedWallet_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletRequest request = new WalletRequest("0x123abc", "mainnet");

        when(userService.addWhitelistedWallet(any(UUID.class), any(WalletRequest.class))).thenReturn(walletId);

        mockMvc.perform(post("/api/wallet/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wallet whitelisted"))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()));

        verify(userService, times(1)).addWhitelistedWallet(any(UUID.class), any(WalletRequest.class));
    }

    @Test
    @WithUserPrincipal(roles = "ROLE_INSTITUTIONAL_USER")
    void getWhitelistedWallets_Success() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setWalletAddress("0x123abc");

        when(userService.getWhitelistedWallets(any(UUID.class))).thenReturn(List.of(wallet));

        mockMvc.perform(get("/api/wallet/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallets[0].address").value("0x123abc"));

        verify(userService, times(1)).getWhitelistedWallets(any(UUID.class));
    }
}
