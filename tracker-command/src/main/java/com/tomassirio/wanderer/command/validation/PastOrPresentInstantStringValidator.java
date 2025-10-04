package com.tomassirio.wanderer.command.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class PastOrPresentInstantStringValidator implements ConstraintValidator<PastOrPresentInstantString, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            Instant parsedInstant = Instant.parse(value);
            return !parsedInstant.isAfter(Instant.now().plusSeconds(300));
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

