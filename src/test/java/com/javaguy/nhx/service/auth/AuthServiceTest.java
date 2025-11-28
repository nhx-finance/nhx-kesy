package com.javaguy.nhx.service.auth;

import com.javaguy.nhx.exception.custom.*;
import com.javaguy.nhx.model.dto.request.LoginRequest;
import com.javaguy.nhx.model.dto.request.SignupRequest;
import com.javaguy.nhx.model.dto.request.VerifyOtpRequest;
import com.javaguy.nhx.model.entity.RefreshToken;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.model.enums.KycStatus;
import com.javaguy.nhx.model.enums.UserRole;
import com.javaguy.nhx.repository.RefreshTokenRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.model.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OtpService otpService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UUID userId;
    private String email = "test@example.com";
    private String password = "password";
    private String otp = "123456";
    private String accessToken = "testAccessToken";
    private String refreshToken = "testRefreshToken";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(false)
                .enabled(false)
                .kycStatus(KycStatus.UNVERIFIED)
                .role(UserRole.INSTITUTIONAL_USER)
                .createdAt(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(authService, "accessTokenExpiry", 900L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiry", 604800L);
    }

    @Test
    void signup_Success() {
        SignupRequest request = new SignupRequest(email, password);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(otpService).sendOtp(email);

        authService.signup(request);

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).sendOtp(email);
    }

    @Test
    void signup_EmailAlreadyInUse_ThrowsConflictException() {
        SignupRequest request = new SignupRequest(email, password);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.signup(request));

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(otpService, never()).sendOtp(anyString());
    }

    @Test
    void signup_OtpSendFails_ThrowsInternalServerException() {
        SignupRequest request = new SignupRequest(email, password);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("OTP send error")).when(otpService).sendOtp(email);

        assertThrows(InternalServerException.class, () -> authService.signup(request));

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).sendOtp(email);
    }

    @Test
    void verifyOtp_Success_UnverifiedEmail() {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        user.setEmailVerified(false);
        doNothing().when(otpService).verifyOtp(email, otp);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken(userId)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken()).thenReturn(refreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.verifyOtp(request);

        assertTrue(user.isEmailVerified());
        assertTrue(user.isEnabled());
        assertNotNull(user.getVerifiedAt());
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(otpService, times(1)).verifyOtp(email, otp);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(user);
        verify(tokenProvider, times(1)).generateToken(userId);
        verify(tokenProvider, times(1)).generateRefreshToken();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void verifyOtp_Success_AlreadyVerifiedEmail() {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        user.setEmailVerified(true);
        user.setEnabled(true);
        doNothing().when(otpService).verifyOtp(email, otp);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).deleteByUser(any(User.class));
        when(tokenProvider.generateToken(userId)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken()).thenReturn(refreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.verifyOtp(request);

        assertTrue(user.isEmailVerified());
        assertTrue(user.isEnabled());
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(otpService, times(1)).verifyOtp(email, otp);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(user); // Should not save if already verified
        verify(refreshTokenRepository, times(1)).deleteByUser(any(User.class));
        verify(tokenProvider, times(1)).generateToken(userId);
        verify(tokenProvider, times(1)).generateRefreshToken();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void verifyOtp_UserNotFound_ThrowsResourceNotFoundException() {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        doNothing().when(otpService).verifyOtp(email, otp);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.verifyOtp(request));

        verify(otpService, times(1)).verifyOtp(email, otp);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(tokenProvider, never()).generateToken(any(UUID.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest(email, password);
        user.setEmailVerified(true);
        user.setEnabled(true);
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, "encodedPassword", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken(userId)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken()).thenReturn(refreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(any(RefreshToken.class));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(user.getLastLoginAt());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(tokenProvider, times(1)).generateToken(userId);
        verify(tokenProvider, times(1)).generateRefreshToken();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_BadCredentials_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest(email, password);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_UserNotFoundAfterAuthentication_ThrowsResourceNotFoundException() {
        LoginRequest request = new LoginRequest(email, password);
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, "encodedPassword", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_EmailNotVerified_ThrowsEmailNotVerifiedException() {
        LoginRequest request = new LoginRequest(email, password);
        user.setEmailVerified(false);
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, "encodedPassword", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(EmailNotVerifiedException.class, () -> authService.login(request));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_AccountDisabled_ThrowsAccountDisabledException() {
        LoginRequest request = new LoginRequest(email, password);
        user.setEmailVerified(true);
        user.setEnabled(false);
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, "encodedPassword", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(AccountDisabledException.class, () -> authService.login(request));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_Success() {
        user.setEnabled(true); // User must be enabled for refresh token to work
        RefreshToken oldRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(oldRefreshToken));
        when(tokenProvider.generateToken(userId)).thenReturn("newAccessToken");
        when(tokenProvider.generateRefreshToken()).thenReturn("newRefreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(refreshTokenRepository).delete(oldRefreshToken);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(tokenProvider, times(1)).generateToken(userId);
        verify(tokenProvider, times(1)).generateRefreshToken();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        verify(refreshTokenRepository, times(1)).delete(oldRefreshToken);
    }

    @Test
    void refreshToken_InvalidToken_ThrowsUnauthorizedException() {
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));

        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(tokenProvider, never()).generateToken(any(UUID.class));
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsUnauthorizedException() {
        RefreshToken oldRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(oldRefreshToken));
        doNothing().when(refreshTokenRepository).delete(oldRefreshToken);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));

        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(refreshTokenRepository, times(1)).delete(oldRefreshToken);
        verify(tokenProvider, never()).generateToken(any(UUID.class));
    }

    @Test
    void refreshToken_AccountDisabled_ThrowsAccountDisabledException() {
        user.setEnabled(false);
        RefreshToken oldRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(oldRefreshToken));
        doNothing().when(refreshTokenRepository).delete(oldRefreshToken);

        assertThrows(AccountDisabledException.class, () -> authService.refreshToken(refreshToken));

        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(refreshTokenRepository, times(1)).delete(oldRefreshToken);
        verify(tokenProvider, never()).generateToken(any(UUID.class));
    }

    @Test
    void logout_Success() {
        RefreshToken tokenToDelete = RefreshToken.builder().token(refreshToken).user(user).build();
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(tokenToDelete));
        doNothing().when(refreshTokenRepository).delete(tokenToDelete);

        authService.logout(refreshToken);

        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(refreshTokenRepository, times(1)).delete(tokenToDelete);
    }

    @Test
    void logout_TokenNotFound_NoAction() {
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        authService.logout(refreshToken);

        verify(refreshTokenRepository, times(1)).findByToken(refreshToken);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void logoutAllDevices_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).deleteByUser(user);

        authService.logoutAllDevices(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    void logoutAllDevices_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.logoutAllDevices(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
    }
}
