package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BaseException {
    public EmailNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
