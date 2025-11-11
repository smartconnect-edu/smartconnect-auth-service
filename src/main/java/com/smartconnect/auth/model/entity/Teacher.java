package com.smartconnect.auth.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * Teacher entity representing teacher profile information
 * Following SRP - Single responsibility for teacher data management
 * Following LSP - Can be substituted wherever BaseEntity is expected
 */
@Entity
@Table(name = "teachers", indexes = {
    @Index(name = "idx_teachers_user_id", columnList = "user_id"),
    @Index(name = "idx_teachers_teacher_code", columnList = "teacher_code"),
    @Index(name = "idx_teachers_faculty_id", columnList = "faculty_id"),
    @Index(name = "idx_teachers_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "teacher_code", unique = true, nullable = false, length = 20)
    private String teacherCode;

    @Column(name = "faculty_id")
    private UUID facultyId;  // Will be FK when Faculty entity exists

    @Column(name = "title", length = 50)
    private String title;  // Professor, Associate Professor, etc.

    @Column(name = "degree", length = 50)
    private String degree;  // PhD, Master, etc.

    @Column(name = "specialization", columnDefinition = "TEXT")
    private String specialization;

    @Column(name = "office", length = 50)
    private String office;

    @Column(name = "office_hours", length = 200)
    private String officeHours;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "research_interests", columnDefinition = "TEXT")
    private String researchInterests;

    @Column(name = "publications_count")
    @Builder.Default
    private Integer publicationsCount = 0;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Business logic: Check if teacher can teach courses
     * Following SRP - Business logic in entity
     */
    public boolean canTeach() {
        return isActive && user.isEnabled();
    }

    /**
     * Business logic: Calculate years of experience
     */
    public Integer getYearsOfExperience() {
        if (hireDate == null) {
            return null;
        }
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    /**
     * Business logic: Check if teacher is senior (>= 10 years experience)
     */
    public boolean isSeniorTeacher() {
        Integer years = getYearsOfExperience();
        return years != null && years >= 10;
    }

    /**
     * Business logic: Check if teacher has PhD
     */
    public boolean hasPhD() {
        return degree != null && (degree.toUpperCase().contains("PHD") || 
                                  degree.toUpperCase().contains("TIẾN SĨ"));
    }

    /**
     * Business logic: Check if teacher is professor level
     */
    public boolean isProfessor() {
        return title != null && (title.toUpperCase().contains("PROFESSOR") ||
                                 title.toUpperCase().contains("GIÁO SƯ"));
    }
}

