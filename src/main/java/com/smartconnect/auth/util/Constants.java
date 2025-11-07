package com.smartconnect.auth.util;

/**
 * Application-wide constants
 */
public final class Constants {
    
    // JWT Constants
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_STRING = "Authorization";
    public static final long JWT_ACCESS_TOKEN_EXPIRATION = 86400000; // 24 hours
    public static final long JWT_REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    
    // API Paths
    public static final String API_V1_PREFIX = "/v1";
    public static final String AUTH_BASE_PATH = API_V1_PREFIX + "/auth";
    public static final String USER_BASE_PATH = API_V1_PREFIX + "/users";
    
    // Public Endpoints (no authentication required)
    public static final String[] PUBLIC_URLS = {
        // Auth endpoints
        AUTH_BASE_PATH + "/login",
        AUTH_BASE_PATH + "/register",
        AUTH_BASE_PATH + "/refresh-token",
        AUTH_BASE_PATH + "/forgot-password",
        AUTH_BASE_PATH + "/reset-password",
        
        // Health & Actuator endpoints
        "/health",
        "/actuator/**",
        
        // Swagger & OpenAPI endpoints
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };
    
    // Redis Keys
    public static final String REDIS_REFRESH_TOKEN_KEY = "refresh_token:";
    public static final String REDIS_BLACKLIST_TOKEN_KEY = "blacklist_token:";
    
    // Validation Messages
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email format is invalid";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters";
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String USERNAME_MIN_LENGTH = "Username must be at least 3 characters";
    
    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    
    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}

