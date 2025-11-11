package com.smartconnect.auth.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin entity representing admin profile information
 * Following SRP - Single responsibility for admin data management
 * Following ISP - Specific permissions interface
 */
@Entity
@Table(name = "admins", indexes = {
    @Index(name = "idx_admins_user_id", columnList = "user_id"),
    @Index(name = "idx_admins_admin_code", columnList = "admin_code"),
    @Index(name = "idx_admins_department", columnList = "department"),
    @Index(name = "idx_admins_is_active", columnList = "is_active"),
    @Index(name = "idx_admins_access_level", columnList = "access_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "admin_code", unique = true, nullable = false, length = 20)
    private String adminCode;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    @Type(JsonBinaryType.class)
    @Column(name = "permissions", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> permissions = new HashMap<>();

    @Column(name = "access_level")
    @Builder.Default
    private Integer accessLevel = 1;  // 1=basic, 2=advanced, 3=super

    @Column(name = "can_manage_users")
    @Builder.Default
    private Boolean canManageUsers = false;

    @Column(name = "can_manage_courses")
    @Builder.Default
    private Boolean canManageCourses = false;

    @Column(name = "can_manage_grades")
    @Builder.Default
    private Boolean canManageGrades = false;

    @Column(name = "can_view_reports")
    @Builder.Default
    private Boolean canViewReports = false;

    @Column(name = "can_manage_system")
    @Builder.Default
    private Boolean canManageSystem = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Business logic: Check if admin is super admin
     * Following SRP - Business logic in entity
     */
    public boolean isSuperAdmin() {
        return accessLevel != null && accessLevel == 3;
    }

    /**
     * Business logic: Check if admin has full access
     */
    public boolean hasFullAccess() {
        return isSuperAdmin() || (canManageUsers && canManageCourses && 
                                  canManageGrades && canManageSystem);
    }

    /**
     * Business logic: Check if admin can perform action
     */
    public boolean canPerformAction(String action) {
        if (!isActive || !user.isEnabled()) {
            return false;
        }
        
        if (isSuperAdmin()) {
            return true;
        }

        return switch (action.toLowerCase()) {
            case "manage_users" -> canManageUsers;
            case "manage_courses" -> canManageCourses;
            case "manage_grades" -> canManageGrades;
            case "view_reports" -> canViewReports;
            case "manage_system" -> canManageSystem;
            default -> false;
        };
    }

    /**
     * Business logic: Check if admin has specific permission
     */
    public boolean hasPermission(String resource, String action) {
        if (isSuperAdmin()) {
            return true;
        }

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        Object resourcePerms = permissions.get(resource);
        if (resourcePerms instanceof java.util.List) {
            return ((java.util.List<?>) resourcePerms).contains(action);
        }

        return false;
    }
}

