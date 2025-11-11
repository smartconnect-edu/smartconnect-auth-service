package com.smartconnect.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Usage: Run this class to generate password hashes for sample data
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin@123";
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + encoder.encode(password));
        System.out.println("\nNote: Each run generates a different hash (due to different salt)");
        System.out.println("But all hashes will match the same password");
    }
}

