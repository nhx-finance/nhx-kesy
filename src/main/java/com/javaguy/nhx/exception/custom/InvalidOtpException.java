package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BaseException {
    public InvalidOtpException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
