package com.smartconnect.auth.service;

import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.service.impl.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtService
 * Tests JWT token generation, validation, and extraction
 */
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    
    // Use a valid base64 encoded secret key (256 bits)
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcy1vbmx5LTEyMzQ1Njc4OTA=";
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000L; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = createJwtService(TEST_SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .fullName("Test User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();
        testUser.setId(UUID.randomUUID());
    }

    private JwtService createJwtService(String secret, long accessExpiration, long refreshExpiration) {
        JwtServiceImpl service = new JwtServiceImpl();
        ReflectionTestUtils.setField(service, "jwtSecret", secret);
        ReflectionTestUtils.setField(service, "accessTokenExpiration", accessExpiration);
        ReflectionTestUtils.setField(service, "refreshTokenExpiration", refreshExpiration);
        return service;
    }

    // ==================== ACCESS TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Should generate access token successfully")
    void shouldGenerateAccessTokenSuccessfully() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Access token should contain user information")
    void accessTokenShouldContainUserInformation() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        String username = jwtService.getUsernameFromToken(token);
        UUID userId = jwtService.getUserIdFromToken(token);

        assertThat(username).isEqualTo(testUser.getUsername());
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Access token should have correct expiration time")
    void accessTokenShouldHaveCorrectExpirationTime() {
        // Given
        long beforeGeneration = System.currentTimeMillis();

        // When
        String token = jwtService.generateAccessToken(testUser);
        Date expirationDate = jwtService.getExpirationDateFromToken(token);

        // Then
        long afterGeneration = System.currentTimeMillis();
        long expectedMinExpiration = beforeGeneration + ACCESS_TOKEN_EXPIRATION;
        long expectedMaxExpiration = afterGeneration + ACCESS_TOKEN_EXPIRATION + 1000; // Add 1 second tolerance
        
        assertThat(expirationDate.getTime())
                .isGreaterThanOrEqualTo(expectedMinExpiration)
                .isLessThanOrEqualTo(expectedMaxExpiration);
    }

    // ==================== REFRESH TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshTokenSuccessfully() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Refresh token should contain user ID")
    void refreshTokenShouldContainUserId() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        UUID userId = jwtService.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Refresh token should have longer expiration than access token")
    void refreshTokenShouldHaveLongerExpiration() {
        // When
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        Date accessExpiration = jwtService.getExpirationDateFromToken(accessToken);
        Date refreshExpiration = jwtService.getExpirationDateFromToken(refreshToken);

        // Then
        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    // ==================== TOKEN EXTRACTION TESTS ====================

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String username = jwtService.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        UUID userId = jwtService.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDateFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Date expirationDate = jwtService.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void shouldExtractCustomClaimFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Then
        assertThat(role).isEqualTo(testUser.getRole().name());
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate token successfully with correct user")
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());

        // When
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should fail validation with wrong username")
    void shouldFailValidationWithWrongUsername() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wronguser");

        // When
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should fail validation with malformed token")
    void shouldFailValidationWithMalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());

        // When
        boolean isValid = jwtService.validateToken(malformedToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should fail validation with null token")
    void shouldFailValidationWithNullToken() {
        // Given
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());

        // When
        boolean isValid = jwtService.validateToken(null, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should fail validation with empty token")
    void shouldFailValidationWithEmptyToken() {
        // Given
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());

        // When
        boolean isValid = jwtService.validateToken("", userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    // ==================== TOKEN EXPIRATION TESTS ====================

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given - create a service with very short expiration
        JwtService shortExpirationService = createJwtService(TEST_SECRET, 1L, 1L); // 1ms

        String token = shortExpirationService.generateAccessToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then
        assertThatThrownBy(() -> shortExpirationService.getUsernameFromToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should not be expired immediately after generation")
    void shouldNotBeExpiredImmediatelyAfterGeneration() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());

        // When
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    // ==================== TOKEN SECURITY TESTS ====================

    @Test
    @DisplayName("Should reject token with invalid signature")
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        
        // Tamper with the signature
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tamperedSignature";

        // When & Then
        assertThatThrownBy(() -> jwtService.getUsernameFromToken(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should reject token signed with different secret")
    void shouldRejectTokenSignedWithDifferentSecret() {
        // Given
        JwtService differentSecretService = createJwtService(
                "ZGlmZmVyZW50LXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi10ZXN0aW5nLXB1cnBvc2VzLW9ubHktdXNlZC1mb3ItdGVzdGluZy1zZWN1cml0eS1mZWF0dXJlcy1vZi1qd3QtbGlicmFyeQ==",
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION
        );

        String token = differentSecretService.generateAccessToken(testUser);

        // When & Then - Should throw exception due to signature mismatch
        assertThatThrownBy(() -> jwtService.getUsernameFromToken(token))
                .isInstanceOf(Exception.class) // Can be SignatureException or other JWT exceptions
                .hasMessageContaining("signature");
    }

    @Test
    @DisplayName("Two tokens for same user should be different")
    void twoTokensForSameUserShouldBeDifferent() {
        // When
        String token1 = jwtService.generateAccessToken(testUser);
        
        // Wait a bit to ensure different issued-at time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    // ==================== GETTER TESTS ====================

    @Test
    @DisplayName("Should return correct access token expiration")
    void shouldReturnCorrectAccessTokenExpiration() {
        // When
        long expiration = jwtService.getAccessTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should return correct refresh token expiration")
    void shouldReturnCorrectRefreshTokenExpiration() {
        // When
        long expiration = jwtService.getRefreshTokenExpiration();

        // Then
        assertThat(expiration).isEqualTo(REFRESH_TOKEN_EXPIRATION);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle user with special characters in username")
    void shouldHandleUserWithSpecialCharactersInUsername() {
        // Given
        testUser.setUsername("user@test.com");
        
        // When
        String token = jwtService.generateAccessToken(testUser);
        String extractedUsername = jwtService.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Should handle user with very long username")
    void shouldHandleUserWithVeryLongUsername() {
        // Given
        String longUsername = "a".repeat(255);
        testUser.setUsername(longUsername);
        
        // When
        String token = jwtService.generateAccessToken(testUser);
        String extractedUsername = jwtService.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(longUsername);
    }

    @Test
    @DisplayName("Should handle different user roles")
    void shouldHandleDifferentUserRoles() {
        // Test all roles
        for (UserRole role : UserRole.values()) {
            // Given
            testUser.setRole(role);
            
            // When
            String token = jwtService.generateAccessToken(testUser);
            String extractedRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

            // Then
            assertThat(extractedRole).isEqualTo(role.name());
        }
    }
}

