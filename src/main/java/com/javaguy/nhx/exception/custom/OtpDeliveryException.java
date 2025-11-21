package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class OtpDeliveryException extends BaseException {
    public OtpDeliveryException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
