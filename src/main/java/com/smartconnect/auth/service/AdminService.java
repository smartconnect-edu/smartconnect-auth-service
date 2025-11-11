package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.request.AdminCreateRequest;
import com.smartconnect.auth.dto.request.AdminUpdateRequest;
import com.smartconnect.auth.dto.response.AdminResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Admin operations
 * Following DIP and ISP principles
 */
public interface AdminService {

    /**
     * Create a new admin profile
     */
    AdminResponse createAdmin(AdminCreateRequest request);

    /**
     * Update existing admin profile
     */
    AdminResponse updateAdmin(UUID id, AdminUpdateRequest request);

    /**
     * Get admin by ID
     */
    AdminResponse getAdminById(UUID id);

    /**
     * Get admin by admin code
     */
    AdminResponse getAdminByCode(String adminCode);

    /**
     * Get admin by user ID
     */
    AdminResponse getAdminByUserId(UUID userId);

    /**
     * Get all admins with pagination
     */
    Page<AdminResponse> getAllAdmins(Pageable pageable);

    /**
     * Get admins by department
     */
    List<AdminResponse> getAdminsByDepartment(String department);

    /**
     * Get admins by access level
     */
    List<AdminResponse> getAdminsByAccessLevel(Integer accessLevel);

    /**
     * Get active admins
     */
    List<AdminResponse> getActiveAdmins();

    /**
     * Get super admins
     */
    List<AdminResponse> getSuperAdmins();

    /**
     * Search admins by keyword
     */
    Page<AdminResponse> searchAdmins(String keyword, Pageable pageable);

    /**
     * Delete admin profile (soft delete)
     */
    void deleteAdmin(UUID id);

    /**
     * Check if admin code exists
     */
    boolean existsByAdminCode(String adminCode);

    /**
     * Count admins by department
     */
    long countAdminsByDepartment(String department);

    /**
     * Count active admins
     */
    long countActiveAdmins();
}

