package com.smartconnect.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for creating a new admin profile
 * Following DTO pattern and validation best practices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new admin profile")
public class AdminCreateRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID to link with admin profile", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotBlank(message = "Admin code is required")
    @Size(min = 5, max = 20, message = "Admin code must be between 5 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Admin code must contain only uppercase letters and numbers")
    @Schema(description = "Unique admin identification code", example = "AD2024001")
    private String adminCode;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department or division", example = "Academic Affairs")
    private String department;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    @Schema(description = "Job position/title", example = "Senior Administrator")
    private String position;

    @Schema(description = "Granular permissions in JSON format", example = "{\"users\": [\"create\", \"read\", \"update\"]}")
    private Map<String, Object> permissions;

    @Min(value = 1, message = "Access level must be between 1 and 3")
    @Max(value = 3, message = "Access level must be between 1 and 3")
    @Schema(description = "Access level: 1=Basic, 2=Advanced, 3=Super", example = "2")
    private Integer accessLevel;

    @Schema(description = "Permission to manage users", example = "true")
    private Boolean canManageUsers;

    @Schema(description = "Permission to manage courses", example = "false")
    private Boolean canManageCourses;

    @Schema(description = "Permission to manage grades", example = "false")
    private Boolean canManageGrades;

    @Schema(description = "Permission to view reports", example = "true")
    private Boolean canViewReports;

    @Schema(description = "Permission to manage system", example = "false")
    private Boolean canManageSystem;

    @Schema(description = "Whether admin is active", example = "true")
    private Boolean isActive;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date of hire", example = "2020-01-15")
    private LocalDate hireDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Internal notes", example = "Responsible for student records management")
    private String notes;
}

