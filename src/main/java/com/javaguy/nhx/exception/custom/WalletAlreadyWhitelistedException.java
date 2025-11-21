package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class WalletAlreadyWhitelistedException extends BaseException {
    public WalletAlreadyWhitelistedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
