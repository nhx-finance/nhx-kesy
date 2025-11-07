package com.javaguy.nhx.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record DetailsRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull @Past LocalDate dob,
    @NotBlank String country,
    @NotBlank String province,
    @NotBlank String timezone,
    @NotNull Boolean termsAgreed
) {}
