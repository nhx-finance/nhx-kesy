package com.javaguy.nhx.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class HederaAccountValidator implements ConstraintValidator<HederaAccount, String> {
    private static final Pattern PATTERN = Pattern.compile("^0\\.0\\.\\d{1,10}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String v = value.trim();
        return !v.isEmpty() && PATTERN.matcher(v).matches();
    }
}
