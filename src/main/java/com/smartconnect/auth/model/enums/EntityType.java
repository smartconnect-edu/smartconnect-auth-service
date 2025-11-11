package com.smartconnect.auth.model.enums;

/**
 * Enum representing types of entities in the system
 * Following OCP - Open for extension, closed for modification
 */
public enum EntityType {
    // User Management
    USER("User account"),
    STUDENT("Student profile"),
    TEACHER("Teacher profile"),
    ADMIN("Admin profile"),
    
    // Academic Management
    COURSE("Course/Subject"),
    CLASS("Class section"),
    ENROLLMENT("Course enrollment"),
    GRADE("Grade record"),
    
    // Content Management
    DOCUMENT("Document/File"),
    NOTIFICATION("Notification"),
    
    // System
    SYSTEM("System configuration");

    private final String description;

    EntityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if entity type is user-related
     */
    public boolean isUserEntity() {
        return this == USER || this == STUDENT || this == TEACHER || this == ADMIN;
    }

    /**
     * Check if entity type is academic-related
     */
    public boolean isAcademicEntity() {
        return this == COURSE || this == CLASS || this == ENROLLMENT || this == GRADE;
    }
}

