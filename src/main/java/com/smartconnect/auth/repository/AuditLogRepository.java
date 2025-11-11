package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.AuditLog;
import com.smartconnect.auth.model.enums.ActionType;
import com.smartconnect.auth.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog entity
 * Following ISP - Interface Segregation Principle (specific methods for audit operations)
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs by user ID
     * Uses @Query to handle null user cases properly
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    Page<AuditLog> findByUser_Id(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find audit logs by action type
     * Using native query to handle PostgreSQL enum type properly
     */
    @Query(value = "SELECT * FROM audit_logs WHERE action::text = :action::text ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM audit_logs WHERE action::text = :action::text",
           nativeQuery = true)
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    /**
     * Find audit logs by entity type
     * Using native query to handle PostgreSQL enum type properly
     */
    @Query(value = "SELECT * FROM audit_logs WHERE entity_type::text = :entityType::text ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM audit_logs WHERE entity_type::text = :entityType::text",
           nativeQuery = true)
    Page<AuditLog> findByEntityType(@Param("entityType") String entityType, Pageable pageable);

    /**
     * Find audit logs by entity ID
     */
    List<AuditLog> findByEntityId(UUID entityId);

    /**
     * Find audit logs by IP address
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * Find audit logs by session ID
     */
    List<AuditLog> findBySessionId(String sessionId);

    /**
     * Find audit logs by date range
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find security-related audit logs
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.action IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE', 'PASSWORD_RESET', 'PERMISSION_CHANGE') ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.action IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE', 'PASSWORD_RESET', 'PERMISSION_CHANGE')")
    Page<AuditLog> findSecurityLogs(Pageable pageable);

    /**
     * Find failed login attempts
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN_FAILED'")
    Page<AuditLog> findFailedLoginAttempts(Pageable pageable);

    /**
     * Find failed login attempts by user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'LOGIN_FAILED' ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedLoginAttemptsByUser(@Param("userId") UUID userId);

    /**
     * Find failed login attempts by user in time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'LOGIN_FAILED' AND a.createdAt >= :since")
    List<AuditLog> findRecentFailedLoginAttempts(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );

    /**
     * Count failed login attempts by user since date
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'LOGIN_FAILED' AND a.createdAt >= :since")
    long countFailedLoginAttemptsSince(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );

    /**
     * Find audit logs by user and action
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action")
    Page<AuditLog> findByUser_IdAndAction(@Param("userId") UUID userId, @Param("action") ActionType action, Pageable pageable);

    /**
     * Find audit logs by entity type and action
     */
    Page<AuditLog> findByEntityTypeAndAction(EntityType entityType, ActionType action, Pageable pageable);

    /**
     * Find modifying actions (CREATE, UPDATE, DELETE)
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.action IN ('CREATE', 'UPDATE', 'DELETE') ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.action IN ('CREATE', 'UPDATE', 'DELETE')")
    Page<AuditLog> findModifyingActions(Pageable pageable);

    /**
     * Find audit logs with errors (status code >= 400)
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.statusCode >= 400 ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.statusCode >= 400")
    Page<AuditLog> findErrorLogs(Pageable pageable);

    /**
     * Search audit logs by description
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AuditLog> searchByDescription(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Get recent activity for user
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    Page<AuditLog> findRecentActivityByUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Delete old audit logs (for cleanup)
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate AND a.action NOT IN ('LOGIN_FAILED', 'PASSWORD_CHANGE', 'PERMISSION_CHANGE')")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count logs by user ID
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    long countByUser_Id(@Param("userId") UUID userId);

    /**
     * Count logs by action type
     * Using native query to handle PostgreSQL enum type properly
     */
    @Query(value = "SELECT COUNT(*) FROM audit_logs WHERE action::text = :action::text", nativeQuery = true)
    long countByAction(@Param("action") String action);

    /**
     * Count logs by entity type
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entityType = :entityType")
    long countByEntityType(@Param("entityType") EntityType entityType);

    /**
     * Get activity statistics for date range
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.action")
    List<Object[]> getActivityStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

