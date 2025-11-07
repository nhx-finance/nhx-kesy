package com.javaguy.nhx.util;

import java.math.BigDecimal;

public final class AppConstants {
    public static final BigDecimal KYC_FEE_AMOUNT = new BigDecimal("20000");
    public static final BigDecimal MIN_MINT_AMOUNT = new BigDecimal("10000000");
    public static final int RESTRICTION_DAYS = 45;
    public static final String TREASURY_WALLET_ADDRESS = "TREASURY_WALLET_PLACEHOLDER";
    public static final int OTP_EXPIRY_MINUTES = 5;
    public static final long JWT_ACCESS_TOKEN_EXPIRY = 900000L; // 15 minutes
    public static final long JWT_REFRESH_TOKEN_EXPIRY = 604800000L; // 7 days

    private AppConstants() {
    }
}
