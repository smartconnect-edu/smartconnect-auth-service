package com.smartconnect.auth.config;

import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * Creates default users when the application starts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        
        createDefaultUsers();
        
        log.info("Data initialization completed.");
    }

    /**
     * Create default users if they don't exist
     */
    private void createDefaultUsers() {
        // Create admin user
        createUserIfNotExists(
            "admin",
            "admin@smartconnect.edu.vn",
            "Admin@123456",
            "System Administrator",
            UserRole.SUPER_ADMIN
        );

        // Create demo student user
        createUserIfNotExists(
            "student_demo",
            "student@smartconnect.edu.vn",
            "Student@123",
            "Demo Student",
            UserRole.STUDENT
        );

        // Create demo teacher user
        createUserIfNotExists(
            "teacher_demo",
            "teacher@smartconnect.edu.vn",
            "Teacher@123",
            "Demo Teacher",
            UserRole.TEACHER
        );
    }

    /**
     * Create a user if it doesn't exist
     */
    private void createUserIfNotExists(
            String username,
            String email,
            String password,
            String fullName,
            UserRole role
    ) {
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            log.debug("User already exists, skipping: {}", username);
            return;
        }

        // Create new user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("Created default user: {} ({})", username, role);
    }
}

