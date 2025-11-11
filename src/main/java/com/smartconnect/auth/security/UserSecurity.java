package com.smartconnect.auth.security;

import com.smartconnect.auth.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security service for User resource ownership checks
 * Used in @PreAuthorize expressions to check if current user owns a user resource
 */
@Slf4j
@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    /**
     * Check if the current authenticated user owns the user resource
     * @param userId The user ID to check
     * @return true if current user ID matches the userId parameter, false otherwise
     */
    public boolean isOwner(UUID userId) {
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

            // Check if current user ID matches the requested user ID
            boolean isOwner = currentUser.getId().equals(userId);
            log.debug("User ownership check: requestedUserId={}, currentUserId={}, isOwner={}", 
                    userId, currentUser.getId(), isOwner);
            return isOwner;
        } catch (Exception e) {
            log.error("Error checking user ownership: {}", e.getMessage());
            return false;
        }
    }
}

