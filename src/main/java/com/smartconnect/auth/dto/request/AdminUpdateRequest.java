package com.smartconnect.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for updating admin profile
 * Following SRP - Separate update DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update admin profile")
public class AdminUpdateRequest {

    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department or division", example = "Academic Affairs")
    private String department;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    @Schema(description = "Job position/title", example = "Senior Administrator")
    private String position;

    @Schema(description = "Granular permissions in JSON format")
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
    @Schema(description = "Internal notes")
    private String notes;
}

