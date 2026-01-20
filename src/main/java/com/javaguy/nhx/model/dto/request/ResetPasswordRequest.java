package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank @Email String email,
    @NotBlank String otp,
    @NotBlank @Size(min = 8, message = "password must be 8 or more characters") String newPassword
) {}
