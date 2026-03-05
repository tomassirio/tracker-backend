package com.tomassirio.wanderer.command.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastOrPresentInstantStringValidator.class)
public @interface PastOrPresentInstantString {
    String message() default "Timestamp must be a valid ISO-8601 date-time in the past or present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
