package com.javaguy.nhx.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    // isValidEmail tests
    @Test
    void isValidEmail_ValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("john.doe123@sub.domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("email@domain.info"));
    }

    @Test
    void isValidEmail_InvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("missing@domain"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail("test@.com"));
        assertFalse(ValidationUtil.isValidEmail("test@example..com"));
    }

    @Test
    void isValidEmail_NullAndEmptyEmail() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(" "));
    }

    // isValidPassword tests
    @Test
    void isValidPassword_ValidPasswords() {
        assertTrue(ValidationUtil.isValidPassword("password123"));
        assertTrue(ValidationUtil.isValidPassword("longpasswordwithnumbers123"));
        assertTrue(ValidationUtil.isValidPassword("12345678"));
    }

    @Test
    void isValidPassword_InvalidPasswords() {
        assertFalse(ValidationUtil.isValidPassword("short"));
        assertFalse(ValidationUtil.isValidPassword("1234567"));
    }

    @Test
    void isValidPassword_NullAndEmptyPassword() {
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword(" "));
    }

    // isValidOtp tests
    @Test
    void isValidOtp_ValidOtps() {
        assertTrue(ValidationUtil.isValidOtp("123456"));
        assertTrue(ValidationUtil.isValidOtp("000000"));
        assertTrue(ValidationUtil.isValidOtp("987654"));
    }

    @Test
    void isValidOtp_InvalidOtps() {
        assertFalse(ValidationUtil.isValidOtp("12345")); // Too short
        assertFalse(ValidationUtil.isValidOtp("1234567")); // Too long
        assertFalse(ValidationUtil.isValidOtp("abcde1")); // Non-digit chars
        assertFalse(ValidationUtil.isValidOtp("12345a")); // Mixed chars
        assertFalse(ValidationUtil.isValidOtp(" 12345")); // Contains space
    }

    @Test
    void isValidOtp_NullAndEmptyOtp() {
        assertFalse(ValidationUtil.isValidOtp(null));
        assertFalse(ValidationUtil.isValidOtp(""));
        assertFalse(ValidationUtil.isValidOtp(" "));
    }
}
