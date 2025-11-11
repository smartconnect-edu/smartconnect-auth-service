package com.smartconnect.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for admin response
 * Following DTO pattern for separation of concerns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin profile response")
public class AdminResponse {

    @Schema(description = "Admin profile ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User information")
    private UserResponse user;

    @Schema(description = "Admin code", example = "AD2024001")
    private String adminCode;

    @Schema(description = "Department", example = "Academic Affairs")
    private String department;

    @Schema(description = "Position", example = "Senior Administrator")
    private String position;

    @Schema(description = "Granular permissions")
    private Map<String, Object> permissions;

    @Schema(description = "Access level", example = "2")
    private Integer accessLevel;

    @Schema(description = "Can manage users", example = "true")
    private Boolean canManageUsers;

    @Schema(description = "Can manage courses", example = "false")
    private Boolean canManageCourses;

    @Schema(description = "Can manage grades", example = "false")
    private Boolean canManageGrades;

    @Schema(description = "Can view reports", example = "true")
    private Boolean canViewReports;

    @Schema(description = "Can manage system", example = "false")
    private Boolean canManageSystem;

    @Schema(description = "Is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Hire date", example = "2020-01-15")
    private LocalDate hireDate;

    @Schema(description = "Internal notes")
    private String notes;

    @Schema(description = "Whether admin is super admin", example = "false")
    private Boolean isSuperAdmin;

    @Schema(description = "Whether admin has full access", example = "false")
    private Boolean hasFullAccess;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Profile last update timestamp")
    private LocalDateTime updatedAt;
}

