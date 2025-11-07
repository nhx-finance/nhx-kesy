package com.javaguy.nhx.service;

import com.javaguy.nhx.model.dto.request.LoginRequest;
import com.javaguy.nhx.model.dto.request.SignupRequest;
import com.javaguy.nhx.model.dto.request.VerifyOtpRequest;
import com.javaguy.nhx.model.dto.response.AuthResponse;
import com.javaguy.nhx.model.entity.RefreshToken;
import com.javaguy.nhx.model.entity.User;
import com.javaguy.nhx.exception.ResourceNotFoundException;
import com.javaguy.nhx.model.enums.UserRole;
import com.javaguy.nhx.repository.RefreshTokenRepository;
import com.javaguy.nhx.repository.UserRepository;
import com.javaguy.nhx.security.JwtTokenProvider;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.util.AppConstants;
import com.javaguy.nhx.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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


    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email address already in use.");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.INSTITUTIONAL_USER)
                .build();
        userRepository.save(user);

        otpService.sendOtp(request.email());
        log.info("User {} registered successfully. OTP sent for verification.", request.email());
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        // Verify the OTP
        otpService.verifyOtp(request.email(), request.otp());

        // Find the user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate tokens
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken();

        long now = System.currentTimeMillis();

        // Save refresh token
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(DateUtil.convertToLocalDateTimeViaInstant(
                        tokenProvider.getExpiryDateFromToken(refreshToken)))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("User {} OTP verified and logged in.", request.email());

        return AuthResponse.builder()
                .userId(userPrincipal.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(now + AppConstants.JWT_ACCESS_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(DateUtil.convertToLocalDateTimeViaInstant(
                        tokenProvider.getExpiryDateFromToken(refreshToken)))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        long now = System.currentTimeMillis();

        log.info("User {} logged in successfully.", request.email());

        return AuthResponse.builder()
                .userId(userPrincipal.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(now + AppConstants.JWT_ACCESS_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Refresh Token"));

        if (existingRefreshToken.getExpiryDate().isBefore(
                DateUtil.convertToLocalDateTimeViaInstant(new java.util.Date()))) {
            refreshTokenRepository.delete(existingRefreshToken);
            throw new IllegalArgumentException("Refresh Token expired. Please login again.");
        }

        User user = existingRefreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshTokenString = tokenProvider.generateRefreshToken();

        refreshTokenRepository.delete(existingRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenString)
                .expiryDate(DateUtil.convertToLocalDateTimeViaInstant(
                        tokenProvider.getExpiryDateFromToken(newRefreshTokenString)))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        long now = System.currentTimeMillis();

        log.info("New access and refresh tokens generated for user {}.", user.getEmail());

        return AuthResponse.builder()
                .userId(userPrincipal.getId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .expiresIn(now + AppConstants.JWT_ACCESS_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
        log.info("User logged out successfully by revoking refresh token.");
    }
}