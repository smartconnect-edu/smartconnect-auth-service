package com.smartconnect.auth.filter;

import com.smartconnect.auth.service.CustomUserDetailsService;
import com.smartconnect.auth.service.JwtService;
import com.smartconnect.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter
 * Tests JWT token extraction and authentication
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    // ==================== SUCCESSFUL AUTHENTICATION TESTS ====================

    @Test
    @DisplayName("Should authenticate successfully with valid token")
    void shouldAuthenticateSuccessfullyWithValidToken() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

        verify(jwtService).getUsernameFromToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).validateToken(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    // ==================== NO TOKEN TESTS ====================

    @Test
    @DisplayName("Should skip authentication when no Authorization header")
    void shouldSkipAuthenticationWhenNoAuthorizationHeader() throws ServletException, IOException {
        // Given - no Authorization header

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService, never()).getUsernameFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when Authorization header does not start with Bearer")
    void shouldSkipAuthenticationWhenAuthorizationHeaderNotBearer() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Basic some-credentials");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService, never()).getUsernameFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when Authorization header is empty after Bearer")
    void shouldSkipAuthenticationWhenAuthorizationHeaderEmptyAfterBearer() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService, never()).getUsernameFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    // ==================== BLACKLISTED TOKEN TESTS ====================

    @Test
    @DisplayName("Should reject blacklisted token")
    void shouldRejectBlacklistedToken() throws ServletException, IOException {
        // Given
        String token = "blacklisted-token";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    // ==================== INVALID TOKEN TESTS ====================

    @Test
    @DisplayName("Should handle invalid token gracefully")
    void shouldHandleInvalidTokenGracefully() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reject token when validation fails")
    void shouldRejectTokenWhenValidationFails() throws ServletException, IOException {
        // Given
        String token = "valid-format-but-invalid-token";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).validateToken(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    // ==================== USER NOT FOUND TESTS ====================

    @Test
    @DisplayName("Should handle user not found gracefully")
    void shouldHandleUserNotFoundGracefully() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "nonexistent";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(new RuntimeException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(userDetailsService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);
    }

    // ==================== ALREADY AUTHENTICATED TESTS ====================

    @Test
    @DisplayName("Should skip authentication when already authenticated")
    void shouldSkipAuthenticationWhenAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        // Set existing authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);

        verify(jwtService).getUsernameFromToken(token);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle null username from token")
    void shouldHandleNullUsernameFromToken() throws ServletException, IOException {
        // Given
        String token = "token-with-null-username";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle empty username from token")
    void shouldHandleEmptyUsernameFromToken() throws ServletException, IOException {
        // Given
        String token = "token-with-empty-username";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() throws ServletException, IOException {
        // Given
        String token = "token-with-special!@#$%^&*()";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle very long token")
    void shouldHandleVeryLongToken() throws ServletException, IOException {
        // Given
        String token = "a".repeat(1000);
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        verify(jwtService).getUsernameFromToken(token);
        verify(filterChain).doFilter(request, response);
    }
}

