package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.ValidationException;
import com.javaguy.nhx.model.dto.request.DetailsRequest;
import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.response.MintResponseDto;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.WithUserPrincipal;
import com.javaguy.nhx.service.mint.MintRequestService;
import com.javaguy.nhx.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;
        @MockitoBean
        private MintRequestService mintRequestService;
        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;
        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;
        @Autowired
        private ObjectMapper objectMapper;

        private UUID userId;
        private DetailsRequest detailsRequest;
        private UserProfileRequest userProfileRequest;
        private UserProfileResponse userProfileResponse;
        private MintResponseDto mintResponseDto;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
                detailsRequest = new DetailsRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST", true);
                userProfileRequest = new UserProfileRequest("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "CA", "PST",
                                true, "1.0");

                userProfileResponse = UserProfileResponse.builder()
                                .userId(userId)
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .country("USA")
                                .province("CA")
                                .timezone("PST")
                                .termsAgreed(true)
                                .termsVersion("1.0")
                                .profileComplete(true)
                                .kycStatus(KycStatus.UNVERIFIED)
                                .build();

                mintResponseDto = MintResponseDto.builder()
                                .id(UUID.randomUUID())
                                .amountKes(BigDecimal.TEN)
                                .status(com.javaguy.nhx.model.enums.MintStatus.PENDING)
                                .dateInitiated(LocalDate.now())
                                .walletAddress("0x123abc")
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitDetails_Authenticated_ReturnsOk() throws Exception {
                doNothing().when(userService).submitDetails(any(UUID.class), any(DetailsRequest.class));

                mockMvc.perform(post("/api/user/details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(detailsRequest))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Details saved"));

                verify(userService, times(1)).submitDetails(any(UUID.class), any(DetailsRequest.class));
        }

        @Test
        void submitDetails_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(post("/api/user/details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(detailsRequest))
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());

                verify(userService, never()).submitDetails(any(UUID.class), any(DetailsRequest.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitDetails_InvalidInput_ReturnsBadRequest() throws Exception {
                DetailsRequest invalidRequest = new DetailsRequest(null, null, null, null, null, null, false); // Invalid
                                                                                                               // input

                mockMvc.perform(post("/api/user/details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).submitDetails(any(UUID.class), any(DetailsRequest.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void submitDetails_UserNotFound_ReturnsNotFound() throws Exception {
                doThrow(new ResourceNotFoundException("User not found"))
                                .when(userService).submitDetails(any(UUID.class), any(DetailsRequest.class));

                mockMvc.perform(post("/api/user/details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(detailsRequest))
                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void saveProfile_Authenticated_ReturnsOk() throws Exception {
                when(userService.saveProfile(any(UUID.class), any(UserProfileRequest.class)))
                                .thenReturn(userProfileResponse);

                mockMvc.perform(post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userProfileRequest))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Profile details saved successfully"))
                                .andExpect(jsonPath("$.profileComplete").value(true));

                verify(userService, times(1)).saveProfile(any(UUID.class), any(UserProfileRequest.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void saveProfile_InvalidInput_ReturnsBadRequest() throws Exception {
                UserProfileRequest invalidRequest = new UserProfileRequest(null, null, null, null, null, null, false,
                                null); // Invalid input

                mockMvc.perform(post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).saveProfile(any(UUID.class), any(UserProfileRequest.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void saveProfile_ValidationException_ReturnsBadRequest() throws Exception {
                when(userService.saveProfile(any(UUID.class), any(UserProfileRequest.class)))
                                .thenThrow(new ValidationException("User must be at least 18 years old"));

                mockMvc.perform(post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userProfileRequest))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void saveProfile_UserNotFound_ReturnsNotFound() throws Exception {
                when(userService.saveProfile(any(UUID.class), any(UserProfileRequest.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userProfileRequest))
                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void saveProfile_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userProfileRequest))
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());

                verify(userService, never()).saveProfile(any(UUID.class), any(UserProfileRequest.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getProfile_Authenticated_ReturnsOk() throws Exception {
                when(userService.getProfile(any(UUID.class))).thenReturn(userProfileResponse);

                mockMvc.perform(get("/api/user"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value(userId.toString()))
                                .andExpect(jsonPath("$.firstName").value("John"));

                verify(userService, times(1)).getProfile(any(UUID.class));
        }

        @Test
        void getProfile_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/api/user"))
                                .andExpect(status().isUnauthorized());

                verify(userService, never()).getProfile(any(UUID.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getProfile_UserNotFound_ReturnsNotFound() throws Exception {
                when(userService.getProfile(any(UUID.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/user"))
                                .andExpect(status().isNotFound());

                verify(userService, times(1)).getProfile(any(UUID.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getAllMints_Authenticated_ReturnsOk() throws Exception {
                when(mintRequestService.getAllMintsForUser(any(UUID.class)))
                                .thenReturn(Collections.singletonList(mintResponseDto));

                mockMvc.perform(get("/api/user/mints"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(mintResponseDto.getId().toString()))
                                .andExpect(jsonPath("$[0].amountKes").value(mintResponseDto.getAmountKes()));

                verify(mintRequestService, times(1)).getAllMintsForUser(any(UUID.class));
        }

        @Test
        void getAllMints_Unauthenticated_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/api/user/mints"))
                                .andExpect(status().isUnauthorized());

                verify(mintRequestService, never()).getAllMintsForUser(any(UUID.class));
        }

        @Test
        @WithUserPrincipal(username = "test@example.com", roles = "USER")
        void getAllMints_UserNotFound_ReturnsNotFound() throws Exception {
                when(mintRequestService.getAllMintsForUser(any(UUID.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/user/mints"))
                                .andExpect(status().isNotFound());

                verify(mintRequestService, times(1)).getAllMintsForUser(any(UUID.class));
        }
}
