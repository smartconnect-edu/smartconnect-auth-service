package com.smartconnect.auth.model.entity;

import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AuditLog entity for comprehensive audit trail
 * Following SRP - Single responsibility for audit logging
 * Following OCP - Open for extension with metadata field
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_logs_entity_id", columnList = "entity_id"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_logs_ip_address", columnList = "ip_address"),
    @Index(name = "idx_audit_logs_session_id", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // Can be null for system actions

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ActionType action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_name")
    private String entityName;

    @Type(JsonBinaryType.class)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;

    @Type(JsonBinaryType.class)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_url", columnDefinition = "TEXT")
    private String requestUrl;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Business logic: Check if action was successful
     */
    public boolean isSuccessful() {
        return statusCode != null && statusCode >= 200 && statusCode < 300;
    }

    /**
     * Business logic: Check if action is security-related
     */
    public boolean isSecurityAction() {
        return action.isSecurityAction();
    }

    /**
     * Business logic: Check if action modified data
     */
    public boolean isModifyingAction() {
        return action.isModifyingAction();
    }

    /**
     * Factory method: Create audit log for login
     * Following Factory Pattern
     */
    public static AuditLog createLoginLog(User user, String ipAddress, String userAgent, boolean success) {
        return AuditLog.builder()
                .user(user)
                .action(success ? ActionType.LOGIN : ActionType.LOGIN_FAILED)
                .entityType(EntityType.USER)
                .entityId(user.getId())
                .entityName(user.getUsername())
                .description(success ? "User logged in successfully" : "Failed login attempt")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .statusCode(success ? 200 : 401)
                .build();
    }

    /**
     * Factory method: Create audit log for logout
     */
    public static AuditLog createLogoutLog(User user, String ipAddress) {
        return AuditLog.builder()
                .user(user)
                .action(ActionType.LOGOUT)
                .entityType(EntityType.USER)
                .entityId(user.getId())
                .entityName(user.getUsername())
                .description("User logged out")
                .ipAddress(ipAddress)
                .statusCode(200)
                .build();
    }

    /**
     * Factory method: Create audit log for entity modification
     */
    public static AuditLog createModificationLog(
            User user, 
            ActionType action, 
            EntityType entityType,
            UUID entityId,
            String entityName,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String ipAddress) {
        
        return AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .oldValues(oldValues)
                .newValues(newValues)
                .description(String.format("%s %s: %s", action.name(), entityType.name(), entityName))
                .ipAddress(ipAddress)
                .statusCode(200)
                .build();
    }
}

