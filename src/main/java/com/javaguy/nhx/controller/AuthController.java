package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.LoginRequest;
import com.javaguy.nhx.model.dto.request.SignupRequest;
import com.javaguy.nhx.model.dto.request.VerifyOtpRequest;
import com.javaguy.nhx.model.dto.response.AuthResponse;
import com.javaguy.nhx.service.AuthService;
import com.javaguy.nhx.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Register a new user", description = "Registers a new user with email and password, and sends an OTP for verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "OTP sent to email",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Email address already in use or invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "OTP sent to email"));
    }

    @Operation(summary = "Verify OTP and login", description = "Verifies the provided OTP and logs in the user, returning JWT and refresh tokens in HTTP-only cookies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified and user logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.verifyOtp(request);

        String accessToken = jwtTokenProvider.generateToken(authResponse.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        ResponseCookie jwtCookie = jwtTokenProvider.generateJwtCookie(accessToken);
        ResponseCookie refreshCookie = jwtTokenProvider.generateJwtRefreshCookie(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }

    @Operation(summary = "User login", description = "Authenticates a user with email and password, returning JWT and refresh tokens in HTTP-only cookies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or bad request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - bad credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        String accessToken = jwtTokenProvider.generateToken(authResponse.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        ResponseCookie jwtCookie = jwtTokenProvider.generateJwtCookie(accessToken);
        ResponseCookie refreshCookie = jwtTokenProvider.generateJwtRefreshCookie(refreshToken);

        log.info("Login successful for user with ID: {}", authResponse.getUserId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }

    @Operation(summary = "Refresh access token", description = "Rotates refresh token and issues a new JWT using the refresh token stored in httpOnly cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);
        String cookieName = jwtTokenProvider.getJwtRefreshCookieName();
        String refreshToken = Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Missing refresh token"));
        }
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateToken(authResponse.getUserId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        ResponseCookie jwtCookie = jwtTokenProvider.generateJwtCookie(newAccessToken);
        ResponseCookie refreshCookie = jwtTokenProvider.generateJwtRefreshCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }

    @Operation(summary = "Logout user", description = "Logs out the currently authenticated user by invalidating their refresh token and clearing JWT cookies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]);
        String refreshCookieName = jwtTokenProvider.getJwtRefreshCookieName();
        Arrays.stream(cookies)
                .filter(c -> refreshCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().ifPresent(authService::logout);

        // Clear cookies
        ResponseCookie jwtCookie = jwtTokenProvider.getCleanJwtCookie();
        ResponseCookie refreshCookie = jwtTokenProvider.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}
