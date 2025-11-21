package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidAgeException extends BaseException {
    public InvalidAgeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
