package com.smartconnect.auth.model.enums;

/**
 * Enum representing student enrollment status
 * Following ISP (Interface Segregation Principle) - specific enum for student status
 */
public enum StudentStatus {
    ACTIVE("Active - Currently enrolled"),
    SUSPENDED("Suspended - Temporarily not allowed to attend"),
    GRADUATED("Graduated - Completed the program"),
    DROPPED("Dropped - Withdrew from the program");

    private final String description;

    StudentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if student can enroll in courses
     */
    public boolean canEnroll() {
        return this == ACTIVE;
    }

    /**
     * Check if student can access academic resources
     */
    public boolean hasAcademicAccess() {
        return this == ACTIVE || this == SUSPENDED;
    }
}

