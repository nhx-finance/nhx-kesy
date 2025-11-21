package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class AccountDisabledException extends BaseException {
    public AccountDisabledException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
