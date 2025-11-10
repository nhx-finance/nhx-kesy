package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BaseException {
    public EmailNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
