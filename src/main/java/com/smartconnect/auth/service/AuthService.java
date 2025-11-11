package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.request.LoginRequest;
import com.smartconnect.auth.dto.request.RefreshTokenRequest;
import com.smartconnect.auth.dto.request.RegisterRequest;
import com.smartconnect.auth.dto.response.AuthResponse;

/**
 * Authentication Service interface
 */
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken, String refreshToken);
}

