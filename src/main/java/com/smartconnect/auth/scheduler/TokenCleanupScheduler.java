package com.smartconnect.auth.scheduler;

import com.smartconnect.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Token Cleanup Scheduler
 * Periodically cleans up expired and revoked tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Delete expired refresh tokens
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Expired refresh tokens cleanup completed");
        } catch (Exception e) {
            log.error("Error during expired tokens cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete revoked tokens older than 30 days
     * Runs every week on Sunday at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupRevokedTokens() {
        log.info("Starting cleanup of old revoked refresh tokens");
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);
            log.info("Old revoked refresh tokens cleanup completed");
        } catch (Exception e) {
            log.error("Error during revoked tokens cleanup: {}", e.getMessage(), e);
        }
    }
}

