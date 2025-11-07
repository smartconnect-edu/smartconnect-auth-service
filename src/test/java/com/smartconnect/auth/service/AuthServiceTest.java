package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.request.LoginRequest;
import com.smartconnect.auth.dto.request.RefreshTokenRequest;
import com.smartconnect.auth.dto.request.RegisterRequest;
import com.smartconnect.auth.dto.response.AuthResponse;
import com.smartconnect.auth.exception.InvalidTokenException;
import com.smartconnect.auth.exception.UnauthorizedException;
import com.smartconnect.auth.exception.UserAlreadyExistsException;
import com.smartconnect.auth.model.entity.RefreshToken;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.repository.RefreshTokenRepository;
import com.smartconnect.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests all authentication and authorization functionality
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID testUserId;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$encodedPassword")
                .fullName("Test User")
                .phone("1234567890")
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();
        testUser.setId(testUserId);

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123!")
                .fullName("New User")
                .phone("9876543210")
                .role(UserRole.STUDENT)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("Password123!")
                .build();

        refreshToken = RefreshToken.builder()
                .token("valid-refresh-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        // Set properties using reflection
        ReflectionTestUtils.setField(authService, "accountLockThreshold", 5);
        ReflectionTestUtils.setField(authService, "accountLockDurationMinutes", 30);
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user = i.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        
        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register user with default role when role not provided")
    void shouldRegisterUserWithDefaultRole() {
        // Given
        registerRequest.setRole(null);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user = i.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.STUDENT));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        
        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should lock account after 5 failed login attempts")
    void shouldLockAccountAfter5FailedAttempts() {
        // Given
        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, times(1)).save(testUser); // Only saves once in handleFailedLogin
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(testUser.getLockedUntil()).isNotNull();
        assertThat(testUser.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should auto-unlock account after lock period expires")
    void shouldAutoUnlockAccountAfterLockPeriod() {
        // Given
        testUser.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        testUser.setFailedLoginAttempts(5);
        
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                .thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getLockedUntil()).isNull();
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
        verify(userRepository, atLeast(2)).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when account is still locked")
    void shouldThrowExceptionWhenAccountIsStillLocked() {
        // Given
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        testUser.setFailedLoginAttempts(5);
        
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(LockedException.class)
                .hasMessageContaining("Account is locked");

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when account is inactive")
    void shouldThrowExceptionWhenAccountIsInactive() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Account is inactive");

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        
        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

        // When
        AuthResponse result = authService.refreshToken(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(refreshToken.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    @DisplayName("Should throw exception when refresh token not found")
    void shouldThrowExceptionWhenRefreshTokenNotFound() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenRepository).findByToken("invalid-token");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is expired")
    void shouldThrowExceptionWhenRefreshTokenIsExpired() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository).findByToken("expired-token");
    }

    @Test
    @DisplayName("Should throw exception when refresh token is revoked")
    void shouldThrowExceptionWhenRefreshTokenIsRevoked() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
        refreshToken.setRevoked(true);
        
        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenRepository).findByToken("revoked-token");
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
        // Given
        String accessToken = "valid-access-token";
        String refreshTokenValue = "valid-refresh-token";
        
        when(refreshTokenRepository.findByToken(refreshTokenValue))
                .thenReturn(Optional.of(refreshToken));
        doNothing().when(tokenBlacklistService).blacklistToken(accessToken);

        // When
        authService.logout(accessToken, refreshTokenValue);

        // Then
        assertThat(refreshToken.getRevoked()).isTrue();
        verify(tokenBlacklistService).blacklistToken(accessToken);
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    @DisplayName("Should handle logout with invalid refresh token gracefully")
    void shouldHandleLogoutWithInvalidRefreshToken() {
        // Given
        String accessToken = "valid-access-token";
        String refreshTokenValue = "invalid-token";
        
        when(refreshTokenRepository.findByToken(refreshTokenValue))
                .thenReturn(Optional.empty());
        doNothing().when(tokenBlacklistService).blacklistToken(accessToken);

        // When
        authService.logout(accessToken, refreshTokenValue);

        // Then
        verify(tokenBlacklistService).blacklistToken(accessToken);
        verify(refreshTokenRepository, never()).save(any());
    }
}
