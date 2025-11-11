package com.smartconnect.auth.model.enums;

/**
 * Enum representing types of actions in audit logs
 * Following SRP - Single responsibility for action type classification
 */
public enum ActionType {
    // CRUD Operations
    CREATE("Create new entity"),
    READ("Read/View entity"),
    UPDATE("Update existing entity"),
    DELETE("Delete entity"),
    
    // Authentication & Authorization
    LOGIN("User login"),
    LOGOUT("User logout"),
    LOGIN_FAILED("Failed login attempt"),
    PASSWORD_CHANGE("Password changed"),
    PASSWORD_RESET("Password reset"),
    PERMISSION_CHANGE("Permission modified"),
    STATUS_CHANGE("Status changed"),
    
    // Data Operations
    EXPORT("Data export"),
    IMPORT("Data import"),
    BACKUP("System backup"),
    RESTORE("System restore");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if action is security-related
     */
    public boolean isSecurityAction() {
        return this == LOGIN || this == LOGOUT || this == LOGIN_FAILED ||
               this == PASSWORD_CHANGE || this == PASSWORD_RESET ||
               this == PERMISSION_CHANGE;
    }

    /**
     * Check if action modifies data
     */
    public boolean isModifyingAction() {
        return this == CREATE || this == UPDATE || this == DELETE ||
               this == IMPORT || this == RESTORE;
    }
}

