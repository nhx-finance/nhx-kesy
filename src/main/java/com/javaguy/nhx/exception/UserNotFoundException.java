package com.javaguy.nhx.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(@NotBlank @Email String s) {
        super(s);
    }
}
