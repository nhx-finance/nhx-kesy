package com.javaguy.nhx.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String accountIsDisabled) {
        super(accountIsDisabled);
    }
}
