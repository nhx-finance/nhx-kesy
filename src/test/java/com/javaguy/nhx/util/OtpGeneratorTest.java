package com.javaguy.nhx.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpGeneratorTest {

    @Test
    void generateOtp_ReturnsSixDigitString() {
        String otp = OtpGenerator.generateOtp();
        assertNotNull(otp);
        assertEquals(6, otp.length());
    }

    @Test
    void generateOtp_ContainsOnlyDigits() {
        String otp = OtpGenerator.generateOtp();
        assertTrue(otp.matches("\\d{6}")); // Ensure it's exactly 6 digits
    }

    @Test
    void generateOtp_MultipleCallsProduceDifferentOtpsUsually() {
        String otp1 = OtpGenerator.generateOtp();
        String otp2 = OtpGenerator.generateOtp();
        String otp3 = OtpGenerator.generateOtp();

        // While not strictly guaranteed due to randomness, it's highly improbable for these to be identical
        // We just assert they are not all the same, providing a weak check for randomness.
        assertFalse(otp1.equals(otp2) && otp2.equals(otp3));
    }
}
