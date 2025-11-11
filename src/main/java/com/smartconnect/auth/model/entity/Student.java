package com.smartconnect.auth.model.entity;

import com.smartconnect.auth.model.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Student entity representing student profile information
 * Following SRP - Single responsibility for student data management
 * Following OCP - Open for extension through inheritance from BaseEntity
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_students_user_id", columnList = "user_id"),
    @Index(name = "idx_students_student_code", columnList = "student_code"),
    @Index(name = "idx_students_major_id", columnList = "major_id"),
    @Index(name = "idx_students_admission_year", columnList = "admission_year"),
    @Index(name = "idx_students_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_code", unique = true, nullable = false, length = 20)
    private String studentCode;

    @Column(name = "major_id")
    private UUID majorId;  // Will be FK when Major entity exists

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "gpa", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal gpa = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "student_status")
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    /**
     * Business logic: Check if student can enroll in courses
     * Following SRP - Business logic in entity
     */
    public boolean canEnroll() {
        return status.canEnroll() && user.isEnabled();
    }

    /**
     * Business logic: Check if student has academic access
     */
    public boolean hasAcademicAccess() {
        return status.hasAcademicAccess() && user.isEnabled();
    }

    /**
     * Business logic: Calculate age from date of birth
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Business logic: Check if GPA is honors level (>= 3.5)
     */
    public boolean isHonorsStudent() {
        return gpa != null && gpa.compareTo(new BigDecimal("3.5")) >= 0;
    }
}

