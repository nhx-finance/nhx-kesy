package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class EmailServiceException extends BaseException {
    public EmailServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
