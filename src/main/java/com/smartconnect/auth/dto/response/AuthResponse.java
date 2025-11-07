package com.smartconnect.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Authentication response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with tokens and user information")
public class AuthResponse {

    @Schema(
        description = "JWT access token",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJDVVNUT01FUiIsImlhdCI6MTYyOTg3NjU0MywiZXhwIjoxNjI5ODgwMTQzfQ.dGhpc2lzYW5hY2Nlc3N0b2tlbmV4YW1wbGU"
    )
    private String accessToken;
    
    @Schema(
        description = "JWT refresh token",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInR5cGUiOiJSRUZSRVNIIiwiaWF0IjoxNjI5ODc2NTQzLCJleHAiOjE2MzA0ODEzNDN9.dGhpc2lzYXJlZnJlc2h0b2tlbmV4YW1wbGU"
    )
    private String refreshToken;
    
    @Builder.Default
    @Schema(
        description = "Token type",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    private String tokenType = "Bearer";
    
    @Schema(
        description = "Access token expiration time in seconds",
        example = "3600"
    )
    private Long expiresIn;
    
    // User info
    @Schema(
        description = "User unique identifier",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID userId;
    
    @Schema(
        description = "Username",
        example = "john_doe"
    )
    private String username;
    
    @Schema(
        description = "Email address",
        example = "john.doe@example.com"
    )
    private String email;
    
    @Schema(
        description = "User role",
        example = "CUSTOMER",
        allowableValues = {"CUSTOMER", "TECHNICIAN", "ADMIN"}
    )
    private String role;
}

