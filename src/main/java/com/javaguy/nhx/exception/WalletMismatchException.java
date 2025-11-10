package com.javaguy.nhx.exception;

import org.springframework.http.HttpStatus;

public class WalletMismatchException extends BaseException {
    public WalletMismatchException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
