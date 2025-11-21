package com.javaguy.nhx.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidDocumentException extends BaseException {
    public InvalidDocumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InvalidDocumentException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}
