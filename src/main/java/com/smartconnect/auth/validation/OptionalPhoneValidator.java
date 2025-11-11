package com.smartconnect.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalPhoneValidator implements ConstraintValidator<OptionalPhone, String> {
    
    private static final String PHONE_PATTERN = "^[0-9]{10,15}$";
    
    @Override
    public void initialize(OptionalPhone constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Allow null or empty string
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        // Validate format if phone is provided
        return phone.matches(PHONE_PATTERN);
    }
}

