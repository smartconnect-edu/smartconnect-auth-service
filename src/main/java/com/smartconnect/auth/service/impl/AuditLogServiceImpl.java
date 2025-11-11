package com.smartconnect.auth.service.impl;

import com.smartconnect.auth.dto.response.AuditLogResponse;
import com.smartconnect.auth.exception.ResourceNotFoundException;
import com.smartconnect.auth.mapper.AuditLogMapper;
import com.smartconnect.auth.model.entity.AuditLog;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import com.smartconnect.auth.repository.AuditLogRepository;
import com.smartconnect.auth.repository.UserRepository;
import com.smartconnect.auth.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuditLogService
 * Following SOLID principles
 * Audit logs are immutable after creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void createAuditLog(
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
    ) {
        log.debug("Creating audit log for user: {}, action: {}", userId, action);

        User user = userRepository.findById(userId).orElse(null);

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .oldValues(oldValues)
                .newValues(newValues)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestMethod(requestMethod)
                .requestUrl(requestUrl)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .sessionId(sessionId)
                .durationMs(durationMs)
                .metadata(metadata)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created successfully");
    }

    @Override
    public AuditLogResponse getAuditLogById(UUID id) {
        log.debug("Fetching audit log by ID: {}", id);

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id.toString()));

        return auditLogMapper.toResponse(auditLog);
    }

    @Override
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        log.debug("Fetching all audit logs with pagination");

        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getAuditLogsByUserId(UUID userId, Pageable pageable) {
        log.debug("Fetching audit logs by user ID: {}", userId);
        
        try {
            return auditLogRepository.findByUser_Id(userId, pageable)
                    .map(auditLogMapper::toResponse);
        } catch (Exception e) {
            log.error("Error fetching audit logs by user ID: {}", userId, e);
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<AuditLogResponse> getAuditLogsByAction(ActionType action, Pageable pageable) {
        log.debug("Fetching audit logs by action: {}", action);
        
        try {
            return auditLogRepository.findByAction(action.name(), pageable)
                    .map(auditLogMapper::toResponse);
        } catch (Exception e) {
            log.error("Error fetching audit logs by action: {}", action, e);
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<AuditLogResponse> getAuditLogsByEntityType(EntityType entityType, Pageable pageable) {
        log.debug("Fetching audit logs by entity type: {}", entityType);
        
        try {
            return auditLogRepository.findByEntityType(entityType.name(), pageable)
                    .map(auditLogMapper::toResponse);
        } catch (Exception e) {
            log.error("Error fetching audit logs by entity type: {}", entityType, e);
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }

    @Override
    public List<AuditLogResponse> getAuditLogsByEntity(EntityType entityType, UUID entityId) {
        log.debug("Fetching audit logs by entity: {} - {}", entityType, entityId);

        return auditLogRepository.findByEntityId(entityId).stream()
                .filter(log -> log.getEntityType() == entityType)
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditLogResponse> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        log.debug("Fetching audit logs by date range: {} to {}", startDate, endDate);

        return auditLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getSecurityAuditLogs(Pageable pageable) {
        log.debug("Fetching security audit logs");

        return auditLogRepository.findSecurityLogs(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getFailedActions(Pageable pageable) {
        log.debug("Fetching failed actions");

        return auditLogRepository.findErrorLogs(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    public long countAuditLogsByUser(UUID userId) {
        return auditLogRepository.countByUser_Id(userId);
    }

    @Override
    public long countAuditLogsByAction(ActionType action) {
        try {
            return auditLogRepository.countByAction(action.name());
        } catch (Exception e) {
            log.error("Error counting audit logs by action: {}", action, e);
            return 0L;
        }
    }

    @Override
    public long countFailedActions() {
        return auditLogRepository.findErrorLogs(Pageable.unpaged()).getTotalElements();
    }
}

