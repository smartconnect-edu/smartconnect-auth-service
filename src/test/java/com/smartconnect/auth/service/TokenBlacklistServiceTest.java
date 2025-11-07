package com.smartconnect.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenBlacklistService
 * Tests token blacklist operations using Redis
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Tests")
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private static final String TOKEN_PREFIX = "blacklist:token:";

    @BeforeEach
    void setUp() {
        // Setup will be done in individual tests as needed
    }

    // ==================== BLACKLIST TOKEN TESTS ====================

    @Test
    @DisplayName("Should blacklist token successfully")
    void shouldBlacklistTokenSuccessfully() {
        // Given
        String token = "valid-jwt-token";
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(token)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When
        tokenBlacklistService.blacklistToken(token);

        // Then
        String expectedKey = TOKEN_PREFIX + token;
        verify(jwtService).getExpirationDateFromToken(token);
        verify(valueOperations).set(
                eq(expectedKey),
                eq("blacklisted"),
                any(Duration.class)
        );
    }

    @Test
    @DisplayName("Should not blacklist expired token")
    void shouldNotBlacklistExpiredToken() {
        // Given
        String token = "expired-token";
        Date expiration = new Date(System.currentTimeMillis() - 1000); // Already expired
        
        when(jwtService.getExpirationDateFromToken(token)).thenReturn(expiration);

        // When
        tokenBlacklistService.blacklistToken(token);

        // Then
        verify(jwtService).getExpirationDateFromToken(token);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Should blacklist multiple tokens")
    void shouldBlacklistMultipleTokens() {
        // Given
        String token1 = "token-1";
        String token2 = "token-2";
        String token3 = "token-3";
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(anyString())).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When
        tokenBlacklistService.blacklistToken(token1);
        tokenBlacklistService.blacklistToken(token2);
        tokenBlacklistService.blacklistToken(token3);

        // Then
        verify(valueOperations, times(3)).set(anyString(), eq("blacklisted"), any(Duration.class));
    }

    @Test
    @DisplayName("Should handle exception when blacklisting token")
    void shouldHandleExceptionWhenBlacklistingToken() {
        // Given
        String token = "problematic-token";
        when(jwtService.getExpirationDateFromToken(token))
                .thenThrow(new RuntimeException("Invalid token"));

        // When & Then - should not throw exception
        assertThatCode(() -> tokenBlacklistService.blacklistToken(token))
                .doesNotThrowAnyException();

        verify(jwtService).getExpirationDateFromToken(token);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    // ==================== CHECK BLACKLIST TESTS ====================

    @Test
    @DisplayName("Should return true for blacklisted token")
    void shouldReturnTrueForBlacklistedToken() {
        // Given
        String token = "blacklisted-token";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    @DisplayName("Should return false for non-blacklisted token")
    void shouldReturnFalseForNonBlacklistedToken() {
        // Given
        String token = "valid-token";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    @DisplayName("Should return false when Redis returns null")
    void shouldReturnFalseWhenRedisReturnsNull() {
        // Given
        String token = "some-token";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.hasKey(key)).thenReturn(null);

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    @DisplayName("Should handle exception when checking blacklist")
    void shouldHandleExceptionWhenCheckingBlacklist() {
        // Given
        String token = "problematic-token";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    // ==================== INTEGRATION SCENARIO TESTS ====================

    @Test
    @DisplayName("Should blacklist and verify token")
    void shouldBlacklistAndVerifyToken() {
        // Given
        String token = "test-token";
        String key = TOKEN_PREFIX + token;
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(token)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        tokenBlacklistService.blacklistToken(token);
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isTrue();
        verify(valueOperations).set(eq(key), eq("blacklisted"), any(Duration.class));
        verify(redisTemplate).hasKey(key);
    }

    // ==================== REMOVE FROM BLACKLIST TESTS ====================

    @Test
    @DisplayName("Should remove token from blacklist")
    void shouldRemoveTokenFromBlacklist() {
        // Given
        String token = "token-to-remove";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.delete(key)).thenReturn(true);

        // When
        tokenBlacklistService.removeFromBlacklist(token);

        // Then
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("Should handle exception when removing from blacklist")
    void shouldHandleExceptionWhenRemovingFromBlacklist() {
        // Given
        String token = "problematic-token";
        String key = TOKEN_PREFIX + token;
        
        when(redisTemplate.delete(key)).thenThrow(new RuntimeException("Redis error"));

        // When & Then - should not throw exception
        assertThatCode(() -> tokenBlacklistService.removeFromBlacklist(token))
                .doesNotThrowAnyException();

        verify(redisTemplate).delete(key);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullTokenGracefully() {
        // When & Then
        assertThatCode(() -> tokenBlacklistService.blacklistToken(null))
                .doesNotThrowAnyException();
        
        assertThatCode(() -> tokenBlacklistService.isTokenBlacklisted(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyTokenGracefully() {
        // When & Then
        assertThatCode(() -> tokenBlacklistService.blacklistToken(""))
                .doesNotThrowAnyException();
        
        assertThatCode(() -> tokenBlacklistService.isTokenBlacklisted(""))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle very long token")
    void shouldHandleVeryLongToken() {
        // Given
        String longToken = "a".repeat(1000);
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(longToken)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When & Then
        assertThatCode(() -> tokenBlacklistService.blacklistToken(longToken))
                .doesNotThrowAnyException();

        verify(valueOperations).set(anyString(), eq("blacklisted"), any(Duration.class));
    }

    @Test
    @DisplayName("Should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() {
        // Given
        String specialToken = "token-with-!@#$%^&*()";
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(specialToken)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When
        tokenBlacklistService.blacklistToken(specialToken);

        // Then
        verify(valueOperations).set(anyString(), eq("blacklisted"), any(Duration.class));
    }

    @Test
    @DisplayName("Should use correct TTL based on token expiration")
    void shouldUseCorrectTTLBasedOnTokenExpiration() {
        // Given
        String token = "test-token";
        long ttlMillis = 7200000L; // 2 hours
        Date expiration = new Date(System.currentTimeMillis() + ttlMillis);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(token)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When
        tokenBlacklistService.blacklistToken(token);

        // Then
        verify(valueOperations).set(
                anyString(),
                eq("blacklisted"),
                argThat(duration -> duration.toMillis() > 0 && duration.toMillis() <= ttlMillis)
        );
    }

    @Test
    @DisplayName("Should handle concurrent blacklist operations")
    void shouldHandleConcurrentBlacklistOperations() {
        // Given
        String token = "concurrent-token";
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtService.getExpirationDateFromToken(token)).thenReturn(expiration);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When - simulate concurrent calls
        tokenBlacklistService.blacklistToken(token);
        tokenBlacklistService.blacklistToken(token);

        // Then - both calls should succeed
        verify(valueOperations, times(2)).set(anyString(), eq("blacklisted"), any(Duration.class));
    }
}
