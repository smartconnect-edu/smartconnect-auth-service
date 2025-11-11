package com.smartconnect.auth.security;

import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Security service for Student resource ownership checks
 * Used in @PreAuthorize expressions to check if current user owns a student profile
 */
@Slf4j
@Component("studentSecurity")
@RequiredArgsConstructor
public class StudentSecurity {

    private final StudentRepository studentRepository;

    /**
     * Check if the current authenticated user owns the student profile
     * @param studentId The student ID to check
     * @return true if current user owns the student profile, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isOwner(UUID studentId) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                log.debug("No authentication found");
                return false;
            }

            // Get user from authentication principal
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null || currentUser.getId() == null) {
                log.debug("No user found in authentication");
                return false;
            }

            // Find student with user relationship fetched and check if user owns it
            return studentRepository.findByIdWithUser(studentId)
                    .map(student -> {
                        if (student.getUser() == null) {
                            return false;
                        }
                        boolean isOwner = student.getUser().getId().equals(currentUser.getId());
                        log.debug("Student ownership check: studentId={}, userId={}, isOwner={}", 
                                studentId, currentUser.getId(), isOwner);
                        return isOwner;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking student ownership: {}", e.getMessage());
            return false;
        }
    }
}

