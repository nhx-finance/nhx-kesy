package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class KycAlreadyVerifiedException extends BaseException {
    public KycAlreadyVerifiedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
