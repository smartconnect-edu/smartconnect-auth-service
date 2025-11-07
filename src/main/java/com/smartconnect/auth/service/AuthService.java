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
import com.smartconnect.auth.repository.RefreshTokenRepository;
import com.smartconnect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * Handles user authentication, registration, and token management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.security.account-lock-threshold:5}")
    private int accountLockThreshold;

    @Value("${app.security.account-lock-duration-minutes:30}")
    private int accountLockDurationMinutes;

    /**
     * User login
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for: {}", request.getUsername());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Auto-unlock account if lock period has expired
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            log.info("Account auto-unlocked after lock period expired: {}", user.getUsername());
        }

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            log.warn("Login attempt for locked account: {}", request.getUsername());
            throw new LockedException("Account is locked due to too many failed login attempts. Please try again later.");
        }

        // Check if account is active
        if (!user.isEnabled()) {
            log.warn("Login attempt for inactive account: {}", request.getUsername());
            throw new UnauthorizedException("Account is inactive. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Reset failed login attempts on successful login
        handleSuccessfulLogin(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(user, refreshToken);

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * User registration
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : com.smartconnect.auth.model.enums.UserRole.STUDENT) // Use provided role or default to CUSTOMER
                .isActive(true)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token to database
        saveRefreshToken(user, refreshToken);

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Refresh access token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        log.debug("Refresh token request");

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .filter(token -> !token.getRevoked())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Check if token is valid
        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        // Get user from token
        User user = refreshToken.getUser();

        // Check if account is active
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is inactive");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Revoke old refresh token
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        // Save new refresh token
        saveRefreshToken(user, newRefreshToken);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Logout user
     * @param accessToken Access token to blacklist
     * @param refreshToken Refresh token to revoke
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        log.debug("Logout request");

        // Blacklist access token
        if (accessToken != null && !accessToken.isEmpty()) {
            tokenBlacklistService.blacklistToken(accessToken);
            log.debug("Access token added to blacklist");
        }

        // Revoke refresh token
        refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.getRevoked())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                    log.info("User logged out successfully");
                });
    }

    /**
     * Handle failed login attempt
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= accountLockThreshold) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(accountLockDurationMinutes));
            log.warn("Account locked due to {} failed login attempts: {}", attempts, user.getUsername());
        }

        userRepository.save(user);
    }

    /**
     * Handle successful login
     */
    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(User user, String token) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtService.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}

