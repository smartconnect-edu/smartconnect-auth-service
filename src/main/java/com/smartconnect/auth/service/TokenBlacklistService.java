package com.smartconnect.auth.service;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void removeFromBlacklist(String token);
}

