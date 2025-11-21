package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class KycNotVerifiedException extends BaseException {
    public KycNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
