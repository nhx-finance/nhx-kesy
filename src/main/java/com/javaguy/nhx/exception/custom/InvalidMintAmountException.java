package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidMintAmountException extends BaseException {
    public InvalidMintAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
