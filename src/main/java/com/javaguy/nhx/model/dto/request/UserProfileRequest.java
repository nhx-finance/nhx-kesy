package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserProfileRequest(
        @NotBlank(message = "firstName is required") String firstName,
        @NotBlank(message = "lastName is required") String lastName,
        @NotNull(message = "dateOfBirth is required") @Past(message = "dateOfBirth must be in the past") LocalDate dateOfBirth,
        @NotBlank(message = "country is required") String country,
        @NotBlank(message = "province is required") String province,
        @NotBlank(message = "timezone is required") String timezone,
        @NotNull(message = "termsAgreed is required") Boolean termsAgreed,
        @NotBlank(message = "termsVersion is required") String termsVersion
) {}
