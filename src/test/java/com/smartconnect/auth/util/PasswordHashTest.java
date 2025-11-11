package com.smartconnect.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
    
    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin@123";
        String hash = encoder.encode(password);
        
        System.out.println("=============================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("=============================================");
        
        // Verify the hash matches
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash matches password: " + matches);
        
        // Test with the existing hash from V8
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash matches password: " + existingMatches);
    }
}

