package com.javaguy.nhx.service;

import com.javaguy.nhx.exception.*;
import com.javaguy.nhx.model.dto.request.LoginRequest;
import com.javaguy.nhx.model.dto.request.SignupRequest;
import com.javaguy.nhx.model.dto.request.VerifyOtpRequest;
import com.javaguy.nhx.model.dto.response.AuthResponse;
import com.javaguy.nhx.model.entity.RefreshToken;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.repository.RefreshTokenRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access-token-expiry}")
    private Long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiry;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        try {
            otpService.sendOtp(request.email());
            log.info("User registered successfully: {}", request.email());
        } catch (Exception e) {
            log.error("Failed to send OTP to {}", request.email(), e);
            throw new OtpDeliveryException("Failed to send verification email");
        }
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.email()));

        if (user.isEmailVerified()) {
            log.warn("Attempt to verify already verified email: {}", request.email());
            revokeAllUserTokens(user);
        } else {
            user.setEmailVerified(true);
            user.setEnabled(true);
            user.setVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Email verified for user: {}", request.email());
        }

        log.info("User logged in after OTP verification: {}", request.email());
        return createTokensForUser(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for: {}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            log.warn("Login attempt with unverified email: {}", request.email());
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        if (!user.isEnabled()) {
            log.warn("Login attempt for disabled account: {}", request.email());
            throw new AccountDisabledException("Account is disabled");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", request.email());
        return createTokensForUser(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        RefreshToken existingToken = refreshTokenRepository
                .findByToken(refreshTokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existingToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(existingToken);
            log.warn("Expired refresh token used: {}", existingToken.getUser().getEmail());
            throw new TokenExpiredException("Refresh token expired. Please login again");
        }

        User user = existingToken.getUser();

        if (!user.isEnabled()) {
            refreshTokenRepository.delete(existingToken);
            throw new AccountDisabledException("User account is disabled");
        }

        String newAccessToken = tokenProvider.generateToken(user.getId());
        String newRefreshToken = tokenProvider.generateRefreshToken();

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshToken)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiry))
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(newToken);
        refreshTokenRepository.delete(existingToken);

        log.info("Tokens refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(accessTokenExpiry)
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenString) {
        refreshTokenRepository.findByToken(refreshTokenString)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("User logged out: {}", token.getUser().getEmail());
                });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        revokeAllUserTokens(user);
        log.info("Logged out user {} from all devices", user.getEmail());
    }


    private AuthResponse createTokensForUser(User user) {
        String accessToken = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken();

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiry))
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiry)
                .role(user.getRole().name())
                .build();
    }

    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}