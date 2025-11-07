package com.javaguy.nhx.exception;

public class KycNotVerifiedException extends RuntimeException {
    public KycNotVerifiedException(String message) {
        super(message);
    }
}
