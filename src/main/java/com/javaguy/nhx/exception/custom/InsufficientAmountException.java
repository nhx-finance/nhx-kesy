package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class InsufficientAmountException extends BaseException {
    public InsufficientAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
