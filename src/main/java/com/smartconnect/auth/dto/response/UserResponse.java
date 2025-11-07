package com.smartconnect.auth.dto.response;

import com.smartconnect.auth.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserResponse {

    @Schema(
        description = "User unique identifier",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id;
    
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
        description = "Full name",
        example = "John Doe"
    )
    private String fullName;
    
    @Schema(
        description = "Phone number",
        example = "0912345678"
    )
    private String phone;
    
    @Schema(
        description = "Avatar URL",
        example = "https://example.com/avatar/john_doe.jpg"
    )
    private String avatarUrl;
    
    @Schema(
        description = "User role",
        example = "CUSTOMER",
        allowableValues = {"CUSTOMER", "TECHNICIAN", "ADMIN"}
    )
    private UserRole role;
    
    @Schema(
        description = "Account active status",
        example = "true"
    )
    private Boolean isActive;
    
    @Schema(
        description = "Email verification status",
        example = "true"
    )
    private Boolean isEmailVerified;
    
    @Schema(
        description = "Last login timestamp",
        example = "2025-11-03T14:30:00"
    )
    private LocalDateTime lastLogin;
    
    @Schema(
        description = "Account creation timestamp",
        example = "2025-11-01T10:00:00"
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Last update timestamp",
        example = "2025-11-03T14:30:00"
    )
    private LocalDateTime updatedAt;
}

