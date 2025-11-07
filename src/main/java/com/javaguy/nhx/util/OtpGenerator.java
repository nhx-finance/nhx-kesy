package com.javaguy.nhx.util;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final String OTP_CHARACTERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARACTERS.charAt(secureRandom.nextInt(OTP_CHARACTERS.length())));
        }
        return otp.toString();
    }
}
