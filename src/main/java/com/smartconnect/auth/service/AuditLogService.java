package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.response.AuditLogResponse;
import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for AuditLog operations
 * Following DIP and ISP principles
 * Audit logs are read-only and created automatically
 */
public interface AuditLogService {

    /**
     * Create audit log entry
     * This is called internally by the system
     */
    void createAuditLog(
            UUID userId,
            ActionType action,
            EntityType entityType,
            UUID entityId,
            String entityName,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String description,
            String ipAddress,
            String userAgent,
            String requestMethod,
            String requestUrl,
            Integer statusCode,
            String errorMessage,
            String sessionId,
            Integer durationMs,
            Map<String, Object> metadata
    );

    /**
     * Get audit log by ID
     */
    AuditLogResponse getAuditLogById(UUID id);

    /**
     * Get all audit logs with pagination
     */
    Page<AuditLogResponse> getAllAuditLogs(Pageable pageable);

    /**
     * Get audit logs by user ID
     */
    Page<AuditLogResponse> getAuditLogsByUserId(UUID userId, Pageable pageable);

    /**
     * Get audit logs by action type
     */
    Page<AuditLogResponse> getAuditLogsByAction(ActionType action, Pageable pageable);

    /**
     * Get audit logs by entity type
     */
    Page<AuditLogResponse> getAuditLogsByEntityType(EntityType entityType, Pageable pageable);

    /**
     * Get audit logs by entity
     */
    List<AuditLogResponse> getAuditLogsByEntity(EntityType entityType, UUID entityId);

    /**
     * Get audit logs by date range
     */
    Page<AuditLogResponse> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Get security-related audit logs
     */
    Page<AuditLogResponse> getSecurityAuditLogs(Pageable pageable);

    /**
     * Get failed actions audit logs
     */
    Page<AuditLogResponse> getFailedActions(Pageable pageable);

    /**
     * Count audit logs by user
     */
    long countAuditLogsByUser(UUID userId);

    /**
     * Count audit logs by action
     */
    long countAuditLogsByAction(ActionType action);

    /**
     * Count failed actions
     */
    long countFailedActions();
}

