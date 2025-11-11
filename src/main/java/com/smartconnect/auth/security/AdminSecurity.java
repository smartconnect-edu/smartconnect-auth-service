package com.smartconnect.auth.security;

import com.smartconnect.auth.model.entity.Admin;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security service for Admin resource ownership checks
 * Used in @PreAuthorize expressions to check if current user owns an admin profile
 */
@Slf4j
@Component("adminSecurity")
@RequiredArgsConstructor
public class AdminSecurity {

    private final AdminRepository adminRepository;

    /**
     * Check if the current authenticated user owns the admin profile
     * @param adminId The admin ID to check
     * @return true if current user owns the admin profile, false otherwise
     */
    public boolean isOwner(UUID adminId) {
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

            // Find admin and check if user owns it
            return adminRepository.findById(adminId)
                    .map(admin -> {
                        if (admin.getUser() == null) {
                            return false;
                        }
                        boolean isOwner = admin.getUser().getId().equals(currentUser.getId());
                        log.debug("Admin ownership check: adminId={}, userId={}, isOwner={}", 
                                adminId, currentUser.getId(), isOwner);
                        return isOwner;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking admin ownership: {}", e.getMessage());
            return false;
        }
    }
}

