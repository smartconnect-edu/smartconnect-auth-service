package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.RefreshToken;
import com.smartconnect.auth.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find valid (non-revoked, non-expired) refresh tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(
        @Param("user") User user, 
        @Param("now") LocalDateTime now
    );

    /**
     * Revoke all refresh tokens for a user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now " +
           "WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Delete expired refresh tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete revoked refresh tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :date")
    void deleteRevokedTokensOlderThan(@Param("date") LocalDateTime date);
}

