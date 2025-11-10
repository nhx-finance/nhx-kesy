package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class PaymentFailedException extends BaseException {
    public PaymentFailedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
