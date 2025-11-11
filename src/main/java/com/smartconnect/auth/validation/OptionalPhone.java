package com.smartconnect.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OptionalPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalPhone {
    String message() default "Phone number must be 10-15 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

