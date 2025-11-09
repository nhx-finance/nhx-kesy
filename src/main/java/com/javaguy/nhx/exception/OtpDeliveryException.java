package com.javaguy.nhx.exception;

public class OtpDeliveryException extends RuntimeException {
    public OtpDeliveryException(String failedToSendVerificationEmail) {
        super(failedToSendVerificationEmail);
    }
}
