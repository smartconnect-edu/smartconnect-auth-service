package com.smartconnect.auth.dto.response;

import com.smartconnect.auth.model.enums.UserRole;
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
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private UserRole role;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

