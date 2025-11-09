package com.javaguy.nhx.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String invalidEmailOrPassword) {
        super(invalidEmailOrPassword);
    }
}
