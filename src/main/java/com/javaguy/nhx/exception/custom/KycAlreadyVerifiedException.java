package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class KycAlreadyVerifiedException extends BaseException {
    public KycAlreadyVerifiedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
