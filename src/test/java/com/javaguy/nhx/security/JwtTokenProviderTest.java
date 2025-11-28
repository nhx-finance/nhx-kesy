package com.javaguy.nhx.security;

import com.javaguy.nhx.config.JwtConfig;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private UUID testUserId;
    private String validSecret = "mySecretKeyThatIsLongEnoughForHS256SignatureAlgorithm1234567890";

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        // Setup config mocks with lenient().when() to allow any stubbing
        lenient().doReturn(validSecret).when(jwtConfig).getSecret();
        lenient().doReturn(86400000L).when(jwtConfig).getAccessTokenExpiry(); // 24 hours
        lenient().doReturn(604800000L).when(jwtConfig).getRefreshTokenExpiry(); // 7 days

        jwtTokenProvider.init();
    }

    @Test
    void testGenerateToken_WithUserId() {
        String token = jwtTokenProvider.generateToken(testUserId);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testGenerateToken_WithAuthentication() {
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = mock(UserPrincipal.class);

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(testUserId);

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(testUserId, jwtTokenProvider.getUserIdFromJWT(token));
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
    }

    @Test
    void testGetUserIdFromJWT() {
        String token = jwtTokenProvider.generateToken(testUserId);
        UUID extractedUserId = jwtTokenProvider.getUserIdFromJWT(token);

        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtTokenProvider.generateToken(testUserId);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        when(jwtConfig.getAccessTokenExpiry()).thenReturn(-1000L); // expired
        jwtTokenProvider.init();

        String expiredToken = jwtTokenProvider.generateToken(testUserId);

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void testGetExpiryDateFromToken() {
        String token = jwtTokenProvider.generateToken(testUserId);
        Date expiryDate = jwtTokenProvider.getExpiryDateFromToken(token);

        assertNotNull(expiryDate);
        assertTrue(expiryDate.after(new Date()));
    }

    @Test
    void testGenerateJwtCookie() {
        String token = "test_token";
        ResponseCookie cookie = jwtTokenProvider.generateJwtCookie(token);

        assertNotNull(cookie);
        assertEquals("accessToken", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void testGenerateJwtRefreshCookie() {
        String refreshToken = "refresh_token";
        ResponseCookie cookie = jwtTokenProvider.generateJwtRefreshCookie(refreshToken);

        assertNotNull(cookie);
        assertEquals("refreshToken", cookie.getName());
        assertEquals(refreshToken, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void testGetJwtRefreshCookieName() {
        String cookieName = jwtTokenProvider.getJwtRefreshCookieName();

        assertEquals("refreshToken", cookieName);
    }

    @Test
    void testGetCleanJwtCookie() {
        ResponseCookie cookie = jwtTokenProvider.getCleanJwtCookie();

        assertNotNull(cookie);
        assertEquals("accessToken", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void testGetCleanJwtRefreshCookie() {
        ResponseCookie cookie = jwtTokenProvider.getCleanJwtRefreshCookie();

        assertNotNull(cookie);
        assertEquals("refreshToken", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void testValidateToken_MalformedJwt() {
        String malformedToken = "malformed";

        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    void testValidateToken_EmptyToken() {
        String emptyToken = "";

        assertFalse(jwtTokenProvider.validateToken(emptyToken));
    }

    @Test
    void testGetUserIdFromJWT_InvalidToken() {
        String invalidToken = "invalid.token.format";

        assertThrows(RuntimeException.class, () -> jwtTokenProvider.getUserIdFromJWT(invalidToken));
    }
}
