package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.model.dto.request.KycSubmissionRequest;
import com.javaguy.nhx.model.dto.response.KycStatusResponse;
import com.javaguy.nhx.model.dto.response.KycSubmissionResponse;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.WithUserPrincipal;
import com.javaguy.nhx.service.kyc.KycService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KycController.class)
class KycControllerTest {

        @Autowired
        private WebApplicationContext context;

        private MockMvc mockMvc;

        @MockitoBean
        private KycService kycService;
        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;
        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;
        @Autowired
        private ObjectMapper objectMapper;

        private UUID userId;
        private KycSubmissionRequest kycSubmissionRequest;
        private MockMultipartFile documentFront;
        private MockMultipartFile documentBack;
        private KycSubmissionResponse kycSubmissionResponse;
        private KycStatusResponse kycStatusResponse;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(context)
                                .apply(springSecurity())
                                .build();

                userId = UUID.randomUUID();
                kycSubmissionRequest = new KycSubmissionRequest();
                kycSubmissionRequest.setFullName("John Doe");
                kycSubmissionRequest.setDob("1990-01-01");
                kycSubmissionRequest.setDocumentType("Passport");
                kycSubmissionRequest.setDocumentNumber("12345");
                documentFront = new MockMultipartFile("documentFront", "front.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "front_content".getBytes());
                documentBack = new MockMultipartFile("documentBack", "back.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "back_content".getBytes());
                kycSubmissionResponse = KycSubmissionResponse.builder().kycId(UUID.randomUUID().toString())
                                .status(KycStatus.SUBMITTED.name()).message("Submitted").build();
                kycStatusResponse = KycStatusResponse.builder().status(KycStatus.PENDING)
                                .documents(Collections.emptyList()).build();
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void initiateKyc_Authenticated_ReturnsAccepted() throws Exception {
                when(kycService.initiateKyc(any(UUID.class)))
                                .thenReturn(Map.of("kycId", userId.toString(), "status", KycStatus.INITIATED.name(),
                                                "message", "KYC initiated. Proceed with document submission."));

                mockMvc.perform(post("/api/kyc/initiate").with(csrf()))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.kycId").value(userId.toString()));
        }

        @Test
        void initiateKyc_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(post("/api/kyc/initiate").with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void initiateKyc_UserNotFound_ReturnsNotFound() throws Exception {
                when(kycService.initiateKyc(any(UUID.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(post("/api/kyc/initiate").with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitKycDocuments_Authenticated_ReturnsAccepted() throws Exception {
                when(kycService.submitDocuments(any(UUID.class), any(KycSubmissionRequest.class), eq(documentFront),
                                eq(documentBack)))
                                .thenReturn(kycSubmissionResponse);

                mockMvc.perform(multipart("/api/kyc/submit")
                                .file(documentFront)
                                .file(documentBack)
                                .file(new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE,
                                                objectMapper.writeValueAsBytes(kycSubmissionRequest)))
                                .with(csrf()))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.status").value(KycStatus.SUBMITTED.name()));
        }

        @Test
        void submitKycDocuments_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(multipart("/api/kyc/submit")
                                .file(documentFront)
                                .file(documentBack)
                                .file(new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE,
                                                objectMapper.writeValueAsBytes(kycSubmissionRequest)))
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitKycDocuments_MissingDocumentFront_ReturnsBadRequest() throws Exception {
                mockMvc.perform(multipart("/api/kyc/submit")
                                .file(documentBack)
                                .file(new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE,
                                                objectMapper.writeValueAsBytes(kycSubmissionRequest)))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitKycDocuments_KycAlreadyVerified_ReturnsConflict() throws Exception {
                when(kycService.submitDocuments(any(UUID.class), any(KycSubmissionRequest.class), eq(documentFront),
                                eq(documentBack)))
                                .thenThrow(new ConflictException("KYC already verified"));

                mockMvc.perform(multipart("/api/kyc/submit")
                                .file(documentFront)
                                .file(documentBack)
                                .file(new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE,
                                                objectMapper.writeValueAsBytes(kycSubmissionRequest)))
                                .with(csrf()))
                                .andExpect(status().isConflict());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitKycDocuments_StorageFailure_ReturnsInternalServerError() throws Exception {
                when(kycService.submitDocuments(any(UUID.class), any(KycSubmissionRequest.class), eq(documentFront),
                                eq(documentBack)))
                                .thenThrow(new InternalServerException("Storage failed"));

                mockMvc.perform(multipart("/api/kyc/submit")
                                .file(documentFront)
                                .file(documentBack)
                                .file(new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE,
                                                objectMapper.writeValueAsBytes(kycSubmissionRequest)))
                                .with(csrf()))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getKycStatus_Authenticated_ReturnsOk() throws Exception {
                when(kycService.getKycStatus(any(UUID.class)))
                                .thenReturn(kycStatusResponse);

                mockMvc.perform(get("/api/kyc/status"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(KycStatus.PENDING.name()));
        }

        @Test
        void getKycStatus_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/api/kyc/status"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getKycStatus_UserNotFound_ReturnsNotFound() throws Exception {
                when(kycService.getKycStatus(any(UUID.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/kyc/status"))
                                .andExpect(status().isNotFound());
        }
}
