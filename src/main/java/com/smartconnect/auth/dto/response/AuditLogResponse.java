package com.smartconnect.auth.dto.response;

import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for audit log response
 * Following DTO pattern - Don't expose internal audit structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log entry response")
public class AuditLogResponse {

    @Schema(description = "Audit log ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User ID who performed the action")
    private UUID userId;

    @Schema(description = "Username who performed the action", example = "john.doe")
    private String username;

    @Schema(description = "Action type", example = "LOGIN")
    private ActionType action;

    @Schema(description = "Entity type", example = "USER")
    private EntityType entityType;

    @Schema(description = "Entity ID")
    private UUID entityId;

    @Schema(description = "Entity name", example = "john.doe")
    private String entityName;

    @Schema(description = "Previous state (for UPDATE/DELETE)")
    private Map<String, Object> oldValues;

    @Schema(description = "New state (for CREATE/UPDATE)")
    private Map<String, Object> newValues;

    @Schema(description = "Human-readable description", example = "User logged in successfully")
    private String description;

    @Schema(description = "IP address", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "User agent")
    private String userAgent;

    @Schema(description = "HTTP method", example = "POST")
    private String requestMethod;

    @Schema(description = "Request URL", example = "/api/v1/auth/login")
    private String requestUrl;

    @Schema(description = "HTTP status code", example = "200")
    private Integer statusCode;

    @Schema(description = "Error message if action failed")
    private String errorMessage;

    @Schema(description = "Session ID")
    private String sessionId;

    @Schema(description = "Action duration in milliseconds", example = "125")
    private Integer durationMs;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Whether action was successful", example = "true")
    private Boolean isSuccessful;

    @Schema(description = "Whether action is security-related", example = "true")
    private Boolean isSecurityAction;

    @Schema(description = "Timestamp when action occurred")
    private LocalDateTime createdAt;
}

