package com.javaguy.nhx.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        // Password must be at least 8 characters long
        return password != null && password.length() >= 8;
    }

    public static boolean isValidOtp(String otp) {
        // OTP must be exactly 6 digits
        return otp != null && otp.matches("\\d{6}");
    }
}
