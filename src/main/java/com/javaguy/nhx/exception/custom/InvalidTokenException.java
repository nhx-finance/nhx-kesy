package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException{
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
