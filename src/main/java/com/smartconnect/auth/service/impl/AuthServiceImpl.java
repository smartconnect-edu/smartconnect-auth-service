package com.smartconnect.auth.service.impl;

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
import com.smartconnect.auth.service.AuthService;
import com.smartconnect.auth.service.JwtService;
import com.smartconnect.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Authentication Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.security.account-lock-threshold:5}")
    private int accountLockThreshold;

    @Value("${app.security.account-lock-duration-minutes:30}")
    private int accountLockDurationMinutes;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for: {}", request.getUsername());

        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            log.info("Account auto-unlocked after lock period expired: {}", user.getUsername());
        }

        if (!user.isAccountNonLocked()) {
            log.warn("Login attempt for locked account: {}", request.getUsername());
            throw new LockedException("Account is locked due to too many failed login attempts. Please try again later.");
        }

        if (!user.isEnabled()) {
            log.warn("Login attempt for inactive account: {}", request.getUsername());
            throw new UnauthorizedException("Account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        handleSuccessfulLogin(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        log.info("User logged in successfully: {}", user.getUsername());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        String phone = request.getPhone();
        if (phone != null && phone.trim().isEmpty()) {
            phone = null;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(phone)
                .role(request.getRole() != null ? request.getRole() : com.smartconnect.auth.model.enums.UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        log.info("User registered successfully: {}", user.getUsername());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        log.debug("Refresh token request");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .filter(token -> !token.getRevoked())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is inactive");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        saveRefreshToken(user, newRefreshToken);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        log.debug("Logout request");

        if (StringUtils.hasText(accessToken)) {
            tokenBlacklistService.blacklistToken(accessToken);
            log.debug("Access token added to blacklist");
        }

        refreshTokenRepository.findByToken(StringUtils.hasText(refreshToken) ? refreshToken : "")
                .filter(token -> !token.getRevoked())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                    log.info("User logged out successfully");
                });
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
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

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= accountLockThreshold) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(accountLockDurationMinutes));
            log.warn("Account locked due to {} failed login attempts: {}", attempts, user.getUsername());
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

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

