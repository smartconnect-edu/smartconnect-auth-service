package com.smartconnect.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new teacher profile
 * Following DTO pattern and validation best practices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new teacher profile")
public class TeacherCreateRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID to link with teacher profile", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotBlank(message = "Teacher code is required")
    @Size(min = 5, max = 20, message = "Teacher code must be between 5 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Teacher code must contain only uppercase letters and numbers")
    @Schema(description = "Unique teacher identification code", example = "GV2024001")
    private String teacherCode;

    @Schema(description = "Faculty/Department ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID facultyId;

    @Size(max = 50, message = "Title must not exceed 50 characters")
    @Schema(description = "Academic title", example = "Associate Professor")
    private String title;

    @Size(max = 50, message = "Degree must not exceed 50 characters")
    @Schema(description = "Highest academic degree", example = "PhD in Computer Science")
    private String degree;

    @Size(max = 1000, message = "Specialization must not exceed 1000 characters")
    @Schema(description = "Area of expertise", example = "Artificial Intelligence, Machine Learning")
    private String specialization;

    @Size(max = 50, message = "Office location must not exceed 50 characters")
    @Schema(description = "Office location", example = "Building A, Room 301")
    private String office;

    @Size(max = 200, message = "Office hours must not exceed 200 characters")
    @Schema(description = "Office hours schedule", example = "Monday 2-4 PM, Wednesday 10-12 AM")
    private String officeHours;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    @Schema(description = "Short biography", example = "Dr. John has 15 years of experience in AI research...")
    private String bio;

    @Size(max = 1000, message = "Research interests must not exceed 1000 characters")
    @Schema(description = "Research interests", example = "Deep Learning, Natural Language Processing")
    private String researchInterests;

    @Min(value = 0, message = "Publications count must be non-negative")
    @Schema(description = "Number of academic publications", example = "25")
    private Integer publicationsCount;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date of hire", example = "2015-09-01")
    private LocalDate hireDate;

    @Schema(description = "Whether teacher is active", example = "true")
    private Boolean isActive;
}

