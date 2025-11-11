package com.smartconnect.auth.exception;

/**
 * Exception thrown when user already has a profile of specific type
 * Following SRP - Specific exception for profile conflicts
 */
public class ProfileAlreadyExistsException extends RuntimeException {
    
    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
    
    public ProfileAlreadyExistsException(String profileType, String userId) {
        super(String.format("User '%s' already has a %s profile", userId, profileType));
    }
}

