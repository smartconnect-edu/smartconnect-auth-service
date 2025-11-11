package com.smartconnect.auth.controller;

import com.smartconnect.auth.dto.response.ApiResponse;
import com.smartconnect.auth.dto.response.AuditLogResponse;
import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import com.smartconnect.auth.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for AuditLog operations
 * Read-only endpoints for audit trail
 */
@RestController
@RequestMapping("/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Log Management", description = "APIs for viewing audit logs (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit log by ID")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable UUID id) {
        AuditLogResponse response = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLogResponse> response = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @userSecurity.isOwner(#userId)")
    @Operation(summary = "Get audit logs by user ID")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getAuditLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByAction(
            @PathVariable ActionType action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity-type/{entityType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by entity type")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByEntityType(
            @PathVariable EntityType entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by entity")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId) {
        List<AuditLogResponse> response = auditLogService.getAuditLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by date range")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getAuditLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/security")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get security-related audit logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getSecurityAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getSecurityAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/failed-actions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get failed actions audit logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getFailedActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditLogService.getFailedActions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/count/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Count audit logs by user")
    public ResponseEntity<ApiResponse<Long>> countAuditLogsByUser(@PathVariable UUID userId) {
        long count = auditLogService.countAuditLogsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Count audit logs by action")
    public ResponseEntity<ApiResponse<Long>> countAuditLogsByAction(@PathVariable ActionType action) {
        long count = auditLogService.countAuditLogsByAction(action);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/failed-actions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Count failed actions")
    public ResponseEntity<ApiResponse<Long>> countFailedActions() {
        long count = auditLogService.countFailedActions();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

