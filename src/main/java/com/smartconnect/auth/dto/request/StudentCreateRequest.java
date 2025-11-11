package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.model.enums.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new student profile
 * Following DTO pattern - Data Transfer Object for request
 * Following validation best practices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new student profile")
public class StudentCreateRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID to link with student profile", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotBlank(message = "Student code is required")
    @Size(min = 5, max = 20, message = "Student code must be between 5 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Student code must contain only uppercase letters and numbers")
    @Schema(description = "Unique student identification code", example = "SV2024001")
    private String studentCode;

    @Schema(description = "Major/Program ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID majorId;

    @NotNull(message = "Admission year is required")
    @Min(value = 1900, message = "Admission year must be after 1900")
    @Max(value = 2100, message = "Admission year must be before 2100")
    @Schema(description = "Year of admission", example = "2024")
    private Integer admissionYear;

    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA must not exceed 4.0")
    @Schema(description = "Grade Point Average (0.0 - 4.0)", example = "3.5")
    private BigDecimal gpa;

    @Schema(description = "Student enrollment status", example = "ACTIVE")
    private StudentStatus status;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "2000-01-15")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Home address", example = "123 Main Street, Hanoi")
    private String address;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Parent/Guardian phone number", example = "+84912345678")
    private String parentPhone;

    @Size(max = 100, message = "Emergency contact must not exceed 100 characters")
    @Schema(description = "Emergency contact information", example = "John Doe - 0987654321")
    private String emergencyContact;
}

