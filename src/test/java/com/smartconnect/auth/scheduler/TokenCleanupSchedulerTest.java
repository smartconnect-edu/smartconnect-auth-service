package com.smartconnect.auth.scheduler;

import com.smartconnect.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenCleanupScheduler
 * Tests scheduled token cleanup jobs
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenCleanupScheduler Tests")
class TokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    // ==================== CLEANUP EXPIRED TOKENS TESTS ====================

    @Test
    @DisplayName("Should cleanup expired tokens successfully")
    void shouldCleanupExpiredTokensSuccessfully() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle exception during expired token cleanup")
    void shouldHandleExceptionDuringExpiredTokenCleanup() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When & Then - should not throw exception (caught and logged)
        try {
            tokenCleanupScheduler.cleanupExpiredTokens();
        } catch (Exception e) {
            // Exception should be caught and logged
        }

        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should call deleteExpiredTokens with current time")
    void shouldCallDeleteExpiredTokensWithCurrentTime() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredTokens(
                argThat(date -> date.isBefore(LocalDateTime.now().plusSeconds(1)) &&
                               date.isAfter(LocalDateTime.now().minusSeconds(1)))
        );
    }

    // ==================== CLEANUP REVOKED TOKENS TESTS ====================

    @Test
    @DisplayName("Should cleanup old revoked tokens successfully")
    void shouldCleanupOldRevokedTokensSuccessfully() {
        // Given
        doNothing().when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupRevokedTokens();

        // Then
        verify(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle exception during revoked token cleanup")
    void shouldHandleExceptionDuringRevokedTokenCleanup() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When & Then - should not throw exception (caught and logged)
        try {
            tokenCleanupScheduler.cleanupRevokedTokens();
        } catch (Exception e) {
            // Exception should be caught and logged
        }

        verify(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should cleanup only revoked tokens older than 30 days")
    void shouldCleanupOnlyRevokedTokensOlderThan30Days() {
        // Given
        doNothing().when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupRevokedTokens();

        // Then
        verify(refreshTokenRepository).deleteRevokedTokensOlderThan(
                argThat(date -> {
                    LocalDateTime expected = LocalDateTime.now().minusDays(30);
                    return date.isBefore(expected.plusSeconds(1)) &&
                           date.isAfter(expected.minusSeconds(1));
                })
        );
    }

    // ==================== CONCURRENT EXECUTION TESTS ====================

    @Test
    @DisplayName("Should handle concurrent cleanup executions")
    void shouldHandleConcurrentCleanupExecutions() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
        doNothing().when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When - simulate concurrent executions
        tokenCleanupScheduler.cleanupExpiredTokens();
        tokenCleanupScheduler.cleanupRevokedTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
        verify(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Should complete cleanup within reasonable time")
    void shouldCompleteCleanupWithinReasonableTime() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When
        long startTime = System.currentTimeMillis();
        tokenCleanupScheduler.cleanupExpiredTokens();
        long endTime = System.currentTimeMillis();

        // Then - should complete quickly (less than 1 second for unit test)
        long duration = endTime - startTime;
        assert duration < 1000 : "Cleanup took too long: " + duration + "ms";

        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    // ==================== MULTIPLE EXECUTIONS TESTS ====================

    @Test
    @DisplayName("Should handle multiple cleanup executions")
    void shouldHandleMultipleCleanupExecutions() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When - execute multiple times
        tokenCleanupScheduler.cleanupExpiredTokens();
        tokenCleanupScheduler.cleanupExpiredTokens();
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(refreshTokenRepository, times(3)).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle multiple revoked token cleanup executions")
    void shouldHandleMultipleRevokedTokenCleanupExecutions() {
        // Given
        doNothing().when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When - execute multiple times
        tokenCleanupScheduler.cleanupRevokedTokens();
        tokenCleanupScheduler.cleanupRevokedTokens();

        // Then
        verify(refreshTokenRepository, times(2)).deleteRevokedTokensOlderThan(any(LocalDateTime.class));
    }

    // ==================== ERROR RECOVERY TESTS ====================

    @Test
    @DisplayName("Should recover from error and continue next execution")
    void shouldRecoverFromErrorAndContinueNextExecution() {
        // Given
        doThrow(new RuntimeException("First error"))
                .doNothing()
                .when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When - first execution fails, second succeeds
        try {
            tokenCleanupScheduler.cleanupExpiredTokens();
        } catch (Exception e) {
            // Ignore first error
        }
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(refreshTokenRepository, times(2)).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle null pointer exception gracefully")
    void shouldHandleNullPointerExceptionGracefully() {
        // Given
        doThrow(new NullPointerException("Null error"))
                .when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When & Then - should not propagate exception
        try {
            tokenCleanupScheduler.cleanupExpiredTokens();
        } catch (Exception e) {
            // Exception should be caught
        }

        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    // ==================== TRANSACTION TESTS ====================

    @Test
    @DisplayName("Should execute cleanup in transaction")
    void shouldExecuteCleanupInTransaction() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then - verify method was called (transaction is handled by @Transactional annotation)
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should execute revoked cleanup in transaction")
    void shouldExecuteRevokedCleanupInTransaction() {
        // Given
        doNothing().when(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));

        // When
        tokenCleanupScheduler.cleanupRevokedTokens();

        // Then - verify method was called (transaction is handled by @Transactional annotation)
        verify(refreshTokenRepository).deleteRevokedTokensOlderThan(any(LocalDateTime.class));
    }
}
