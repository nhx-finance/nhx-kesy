package com.javaguy.nhx.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertNotNull(passwordEncoder);
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordEncodingWithBCrypt() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "myPassword123!@#";
        String encodedPassword1 = encoder.encode(password);
        String encodedPassword2 = encoder.encode(password);

        // BCrypt produces different hashes for the same password
        assertNotEquals(encodedPassword1, encodedPassword2);

        // But both should match the password
        assertTrue(encoder.matches(password, encodedPassword1));
        assertTrue(encoder.matches(password, encodedPassword2));
    }

    @Test
    void testPasswordEncodingValidation() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "testPassword";
        String encodedPassword = encoder.encode(password);

        assertTrue(encoder.matches(password, encodedPassword));
        assertFalse(encoder.matches("wrongPassword", encodedPassword));
    }
}
