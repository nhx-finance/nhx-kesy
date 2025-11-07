package com.javaguy.nhx.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = HederaAccountValidator.class)
public @interface HederaAccount {
    String message() default "Invalid Hedera account format. Expected 0.0.XXXXX";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
