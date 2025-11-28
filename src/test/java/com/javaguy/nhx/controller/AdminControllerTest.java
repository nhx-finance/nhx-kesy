package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.model.dto.request.UpdateKycStatusRequest;
import com.javaguy.nhx.model.dto.request.UpdateMintStatusRequest;
import com.javaguy.nhx.model.dto.response.KycSubmissionAdminResponse;
import com.javaguy.nhx.model.dto.response.MintAdminResponse;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.MintStatus;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.WithUserPrincipal;
import com.javaguy.nhx.service.admin.AdminService;
import com.javaguy.nhx.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AdminService adminService;
        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;
        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;
        @Autowired
        private ObjectMapper objectMapper;

        private final UUID kycId = UUID.randomUUID();
        private final UUID userId = UUID.randomUUID();
        private final UUID mintId = UUID.randomUUID();

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void getAllKycSubmissions_NoStatus_ReturnsOk() throws Exception {
                KycSubmissionAdminResponse response = KycSubmissionAdminResponse.builder()
                                .kycId(kycId)
                                .userId(userId)
                                .userEmail("test@example.com")
                                .status(KycStatus.PENDING)
                                .build();
                PageImpl<KycSubmissionAdminResponse> page = new PageImpl<>(Collections.singletonList(response));

                when(adminService.getAllKycSubmissions(eq(null), anyInt(), anyInt()))
                                .thenReturn(page);

                mockMvc.perform(get("/api/admin/kyc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].kycId").value(kycId.toString()))
                                .andExpect(jsonPath("$.content[0].status").value(KycStatus.PENDING.name()));
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void getAllKycSubmissions_WithStatus_ReturnsOk() throws Exception {
                KycSubmissionAdminResponse response = KycSubmissionAdminResponse.builder()
                                .kycId(kycId)
                                .userId(userId)
                                .userEmail("test@example.com")
                                .status(KycStatus.VERIFIED)
                                .build();
                PageImpl<KycSubmissionAdminResponse> page = new PageImpl<>(Collections.singletonList(response));

                when(adminService.getAllKycSubmissions(eq(KycStatus.VERIFIED), anyInt(), anyInt()))
                                .thenReturn(page);

                mockMvc.perform(get("/api/admin/kyc")
                                .param("status", KycStatus.VERIFIED.name()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].kycId").value(kycId.toString()))
                                .andExpect(jsonPath("$.content[0].status").value(KycStatus.VERIFIED.name()));
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void updateKycStatus_ReturnsOk() throws Exception {
                UpdateKycStatusRequest request = new UpdateKycStatusRequest();
                request.setStatus(KycStatus.VERIFIED);
                request.setReviewerNotes("Approved by admin");

                doNothing().when(adminService).updateKycStatus(eq(kycId), any(UpdateKycStatusRequest.class));

                mockMvc.perform(patch("/api/admin/kyc/{kycId}/status", kycId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void getAllMints_NoStatus_ReturnsOk() throws Exception {
                MintAdminResponse response = MintAdminResponse.builder()
                                .requestId(mintId)
                                .userId(userId)
                                .userEmail("test@example.com")
                                .status(MintStatus.PENDING)
                                .build();
                PageImpl<MintAdminResponse> page = new PageImpl<>(Collections.singletonList(response));

                when(adminService.getAllMints(eq(null), anyInt(), anyInt()))
                                .thenReturn(page);

                mockMvc.perform(get("/api/admin/mints"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].requestId").value(mintId.toString()))
                                .andExpect(jsonPath("$.content[0].status").value(MintStatus.PENDING.name()));
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void getAllMints_WithStatus_ReturnsOk() throws Exception {
                MintAdminResponse response = MintAdminResponse.builder()
                                .requestId(mintId)
                                .userId(userId)
                                .userEmail("test@example.com")
                                .status(MintStatus.CONFIRMED)
                                .build();
                PageImpl<MintAdminResponse> page = new PageImpl<>(Collections.singletonList(response));

                when(adminService.getAllMints(eq(MintStatus.CONFIRMED), anyInt(), anyInt()))
                                .thenReturn(page);

                mockMvc.perform(get("/api/admin/mints")
                                .param("status", MintStatus.CONFIRMED.name()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].requestId").value(mintId.toString()))
                                .andExpect(jsonPath("$.content[0].status").value(MintStatus.CONFIRMED.name()));
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_ADMIN")
        void updateMintStatus_ReturnsOk() throws Exception {
                UpdateMintStatusRequest request = new UpdateMintStatusRequest();
                request.setStatus(MintStatus.MINTED);
                request.setNotes("Minted successfully");

                doNothing().when(adminService).updateMintStatus(eq(mintId), any(UpdateMintStatusRequest.class));

                mockMvc.perform(patch("/api/admin/mints/{mintId}/status", mintId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @WithUserPrincipal(roles = "ROLE_USER") // Mock a user with a non-ADMIN role
        void adminEndpoint_AccessDenied_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/api/admin/kyc"))
                                .andExpect(status().isForbidden());
        }
}
