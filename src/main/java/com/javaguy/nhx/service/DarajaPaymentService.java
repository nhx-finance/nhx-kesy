package com.javaguy.nhx.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DarajaPaymentService {

    public Map<String, String> initiateSTKPush(String phoneNumber, BigDecimal amount) {
        // TODO: Implement actual M-Pesa STK Push initiation
        return Map.of("paymentId", "dummy-payment-id-" + System.currentTimeMillis(),
                      "qrLink", "https://dummy-qr-link.com");
    }
}
