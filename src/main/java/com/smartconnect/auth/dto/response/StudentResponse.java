package com.smartconnect.auth.dto.response;

import com.smartconnect.auth.model.enums.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for student response
 * Following DTO pattern - Separate response from entity (security & flexibility)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student profile response")
public class StudentResponse {

    @Schema(description = "Student profile ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User information")
    private UserResponse user;

    @Schema(description = "Student code", example = "SV2024001")
    private String studentCode;

    @Schema(description = "Major/Program ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID majorId;

    @Schema(description = "Admission year", example = "2024")
    private Integer admissionYear;

    @Schema(description = "Grade Point Average", example = "3.5")
    private BigDecimal gpa;

    @Schema(description = "Enrollment status", example = "ACTIVE")
    private StudentStatus status;

    @Schema(description = "Date of birth", example = "2000-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Age calculated from date of birth", example = "24")
    private Integer age;

    @Schema(description = "Home address", example = "123 Main Street, Hanoi")
    private String address;

    @Schema(description = "Parent phone number", example = "+84912345678")
    private String parentPhone;

    @Schema(description = "Emergency contact", example = "John Doe - 0987654321")
    private String emergencyContact;

    @Schema(description = "Whether student can enroll in courses", example = "true")
    private Boolean canEnroll;

    @Schema(description = "Whether student is honors student (GPA >= 3.5)", example = "true")
    private Boolean isHonorsStudent;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Profile last update timestamp")
    private LocalDateTime updatedAt;
}

