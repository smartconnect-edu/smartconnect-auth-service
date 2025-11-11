package com.smartconnect.auth.mapper;

import com.smartconnect.auth.dto.response.AuditLogResponse;
import com.smartconnect.auth.model.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for AuditLog entity and DTOs
 * Following DIP - Dependency Inversion Principle
 * Audit logs are read-only, so no create/update mappers needed
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Map entity to response
     * Including calculated fields
     * Handles null user (for system actions)
     */
    @Mapping(target = "userId", expression = "java(auditLog.getUser() != null ? auditLog.getUser().getId() : null)")
    @Mapping(target = "username", expression = "java(auditLog.getUser() != null ? auditLog.getUser().getUsername() : null)")
    @Mapping(target = "isSuccessful", expression = "java(auditLog.isSuccessful())")
    @Mapping(target = "isSecurityAction", expression = "java(auditLog.isSecurityAction())")
    AuditLogResponse toResponse(AuditLog auditLog);
}

