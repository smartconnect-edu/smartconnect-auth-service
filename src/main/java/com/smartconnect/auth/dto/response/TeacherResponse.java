package com.smartconnect.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for teacher response
 * Following DTO pattern for separation of concerns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Teacher profile response")
public class TeacherResponse {

    @Schema(description = "Teacher profile ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User information")
    private UserResponse user;

    @Schema(description = "Teacher code", example = "GV2024001")
    private String teacherCode;

    @Schema(description = "Faculty/Department ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID facultyId;

    @Schema(description = "Academic title", example = "Associate Professor")
    private String title;

    @Schema(description = "Highest academic degree", example = "PhD in Computer Science")
    private String degree;

    @Schema(description = "Area of expertise", example = "Artificial Intelligence, Machine Learning")
    private String specialization;

    @Schema(description = "Office location", example = "Building A, Room 301")
    private String office;

    @Schema(description = "Office hours", example = "Monday 2-4 PM, Wednesday 10-12 AM")
    private String officeHours;

    @Schema(description = "Biography")
    private String bio;

    @Schema(description = "Research interests")
    private String researchInterests;

    @Schema(description = "Number of publications", example = "25")
    private Integer publicationsCount;

    @Schema(description = "Date of hire", example = "2015-09-01")
    private LocalDate hireDate;

    @Schema(description = "Years of experience", example = "9")
    private Integer yearsOfExperience;

    @Schema(description = "Whether teacher is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether teacher can teach", example = "true")
    private Boolean canTeach;

    @Schema(description = "Whether teacher is senior (>= 10 years)", example = "false")
    private Boolean isSeniorTeacher;

    @Schema(description = "Whether teacher has PhD", example = "true")
    private Boolean hasPhD;

    @Schema(description = "Whether teacher is professor level", example = "true")
    private Boolean isProfessor;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Profile last update timestamp")
    private LocalDateTime updatedAt;
}

