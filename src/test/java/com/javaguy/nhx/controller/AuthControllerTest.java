package com.javaguy.nhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaguy.nhx.exception.custom.AccountDisabledException;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.EmailNotVerifiedException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.UnauthorizedException;
import com.javaguy.nhx.model.dto.request.LoginRequest;
import com.javaguy.nhx.model.dto.request.SignupRequest;
import com.javaguy.nhx.model.dto.request.VerifyOtpRequest;
import com.javaguy.nhx.model.dto.response.AuthResponse;
import com.javaguy.nhx.security.CustomUserDetailsService;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.service.auth.AuthService;
import com.javaguy.nhx.config.TestSecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        private SignupRequest signupRequest;
        private VerifyOtpRequest verifyOtpRequest;
        private LoginRequest loginRequest;
        private AuthResponse authResponse;
        private String accessToken = "mockAccessToken";
        private String refreshToken = "mockRefreshToken";
        private UUID userId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                signupRequest = new SignupRequest("test@example.com", "password123");
                verifyOtpRequest = new VerifyOtpRequest("test@example.com", "123456");
                loginRequest = new LoginRequest("test@example.com", "password123");

                authResponse = AuthResponse.builder()
                                .userId(userId)
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .expiresIn(900L)
                                .role("USER")
                                .build();

                // Mock JwtTokenProvider cookie generation
                when(jwtTokenProvider.generateJwtCookie(anyString()))
                                .thenReturn(ResponseCookie.from("jwt-access-token", accessToken).build());
                when(jwtTokenProvider.generateJwtRefreshCookie(anyString()))
                                .thenReturn(ResponseCookie.from("jwt-refresh-token", refreshToken).build());
                when(jwtTokenProvider.getCleanJwtCookie())
                                .thenReturn(ResponseCookie.from("jwt-access-token", "").maxAge(0).build());
                when(jwtTokenProvider.getCleanJwtRefreshCookie())
                                .thenReturn(ResponseCookie.from("jwt-refresh-token", "").maxAge(0).build());
                when(jwtTokenProvider.getJwtRefreshCookieName()).thenReturn("jwt-refresh-token");
        }

        @Test
        void signup_Success_ReturnsCreated() throws Exception {
                doNothing().when(authService).signup(any(SignupRequest.class));

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("OTP sent to email"));

                verify(authService, times(1)).signup(any(SignupRequest.class));
        }

        @Test
        void signup_InvalidInput_ReturnsBadRequest() throws Exception {
                SignupRequest invalidRequest = new SignupRequest("invalid-email", "short");

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).signup(any(SignupRequest.class));
        }

        @Test
        void signup_EmailConflict_ReturnsConflict() throws Exception {
                doThrow(new ConflictException("Email already in use"))
                                .when(authService).signup(any(SignupRequest.class));

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isConflict());

                verify(authService, times(1)).signup(any(SignupRequest.class));
        }

        @Test
        void verifyOtp_Success_ReturnsOkWithCookies() throws Exception {
                when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(authResponse);

                mockMvc.perform(post("/api/auth/verify-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt-access-token"))
                                .andExpect(cookie().exists("jwt-refresh-token"))
                                .andExpect(jsonPath("$.userId").value(userId.toString()));

                verify(authService, times(1)).verifyOtp(any(VerifyOtpRequest.class));
        }

        @Test
        void verifyOtp_InvalidInput_ReturnsBadRequest() throws Exception {
                VerifyOtpRequest invalidRequest = new VerifyOtpRequest("invalid-email", "123");

                mockMvc.perform(post("/api/auth/verify-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).verifyOtp(any(VerifyOtpRequest.class));
        }

        @Test
        void verifyOtp_Unauthorized_ReturnsUnauthorized() throws Exception {
                doThrow(new UnauthorizedException("Invalid OTP"))
                                .when(authService).verifyOtp(any(VerifyOtpRequest.class));

                mockMvc.perform(post("/api/auth/verify-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                                .andExpect(status().isUnauthorized());

                verify(authService, times(1)).verifyOtp(any(VerifyOtpRequest.class));
        }

        @Test
        void verifyOtp_UserNotFound_ReturnsNotFound() throws Exception {
                doThrow(new ResourceNotFoundException("User not found"))
                                .when(authService).verifyOtp(any(VerifyOtpRequest.class));

                mockMvc.perform(post("/api/auth/verify-otp")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                                .andExpect(status().isNotFound());

                verify(authService, times(1)).verifyOtp(any(VerifyOtpRequest.class));
        }

        @Test
        void login_Success_ReturnsOkWithCookies() throws Exception {
                when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt-access-token"))
                                .andExpect(cookie().exists("jwt-refresh-token"))
                                .andExpect(jsonPath("$.userId").value(userId.toString()));

                verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        void login_InvalidInput_ReturnsBadRequest() throws Exception {
                LoginRequest invalidRequest = new LoginRequest("invalid-email", "short");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(authService, never()).login(any(LoginRequest.class));
        }

        @Test
        void login_BadCredentials_ReturnsUnauthorized() throws Exception {
                doThrow(new UnauthorizedException("Invalid email or password"))
                                .when(authService).login(any(LoginRequest.class));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());

                verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        void login_EmailNotVerified_ReturnsForbidden() throws Exception {
                doThrow(new EmailNotVerifiedException("Please verify"))
                                .when(authService).login(any(LoginRequest.class));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());

                verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        void login_AccountDisabled_ReturnsForbidden() throws Exception {
                doThrow(new AccountDisabledException("Account disabled"))
                                .when(authService).login(any(LoginRequest.class));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());

                verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        void refresh_Success_ReturnsOkWithCookies() throws Exception {
                when(authService.refreshToken(anyString())).thenReturn(authResponse);

                Cookie refreshCookie = new Cookie(jwtTokenProvider.getJwtRefreshCookieName(), refreshToken);

                mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt-access-token"))
                                .andExpect(cookie().exists("jwt-refresh-token"))
                                .andExpect(jsonPath("$.userId").value(userId.toString()));

                verify(authService, times(1)).refreshToken(eq(refreshToken));
        }

        @Test
        void refresh_MissingRefreshToken_ReturnsUnauthorized() throws Exception {
                mockMvc.perform(post("/api/auth/refresh")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Missing refresh token"));

                verify(authService, never()).refreshToken(anyString());
        }

        @Test
        void refresh_InvalidRefreshToken_ReturnsUnauthorized() throws Exception {
                doThrow(new UnauthorizedException("Invalid refresh token"))
                                .when(authService).refreshToken(anyString());

                Cookie refreshCookie = new Cookie(jwtTokenProvider.getJwtRefreshCookieName(), "invalidToken");

                mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie)
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());

                verify(authService, times(1)).refreshToken(eq("invalidToken"));
        }

        @Test
        void logout_Success_ClearsCookies() throws Exception {
                Cookie refreshCookie = new Cookie(jwtTokenProvider.getJwtRefreshCookieName(), refreshToken);
                doNothing().when(authService).logout(anyString());

                mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(cookie().maxAge("jwt-access-token", 0))
                                .andExpect(cookie().maxAge("jwt-refresh-token", 0))
                                .andExpect(jsonPath("$.message").value("Logged out successfully"));

                verify(authService, times(1)).logout(eq(refreshToken));
        }

        @Test
        void logout_NoRefreshToken_StillClearsCookies() throws Exception {
                mockMvc.perform(post("/api/auth/logout")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(cookie().maxAge("jwt-access-token", 0))
                                .andExpect(cookie().maxAge("jwt-refresh-token", 0))
                                .andExpect(jsonPath("$.message").value("Logged out successfully"));

                verify(authService, never()).logout(anyString()); // logout service not called if token is null
        }
}
