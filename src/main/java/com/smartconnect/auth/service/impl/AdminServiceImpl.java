package com.smartconnect.auth.service.impl;

import com.smartconnect.auth.dto.request.AdminCreateRequest;
import com.smartconnect.auth.dto.request.AdminUpdateRequest;
import com.smartconnect.auth.dto.response.AdminResponse;
import com.smartconnect.auth.exception.DuplicateResourceException;
import com.smartconnect.auth.exception.ProfileAlreadyExistsException;
import com.smartconnect.auth.exception.ResourceNotFoundException;
import com.smartconnect.auth.mapper.AdminMapper;
import com.smartconnect.auth.model.entity.Admin;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.repository.AdminRepository;
import com.smartconnect.auth.repository.UserRepository;
import com.smartconnect.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AdminService
 * Following SOLID principles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    @Override
    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {
        log.info("Creating admin profile for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        if (adminRepository.existsByUserId(request.getUserId())) {
            throw new ProfileAlreadyExistsException("Admin", request.getUserId().toString());
        }

        if (adminRepository.existsByAdminCode(request.getAdminCode())) {
            throw new DuplicateResourceException("Admin", request.getAdminCode());
        }

        Admin admin = adminMapper.toEntity(request);
        admin.setUser(user);

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Successfully created admin profile with ID: {}", savedAdmin.getId());

        return adminMapper.toResponse(savedAdmin);
    }

    @Override
    @Transactional
    public AdminResponse updateAdmin(UUID id, AdminUpdateRequest request) {
        log.info("Updating admin profile with ID: {}", id);

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id.toString()));

        adminMapper.updateEntityFromRequest(request, admin);

        Admin updatedAdmin = adminRepository.save(admin);
        log.info("Successfully updated admin profile with ID: {}", id);

        return adminMapper.toResponse(updatedAdmin);
    }

    @Override
    public AdminResponse getAdminById(UUID id) {
        log.debug("Fetching admin by ID: {}", id);

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id.toString()));

        return adminMapper.toResponse(admin);
    }

    @Override
    public AdminResponse getAdminByCode(String adminCode) {
        log.debug("Fetching admin by code: {}", adminCode);

        Admin admin = adminRepository.findByAdminCode(adminCode)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "adminCode", adminCode));

        return adminMapper.toResponse(admin);
    }

    @Override
    public AdminResponse getAdminByUserId(UUID userId) {
        log.debug("Fetching admin by user ID: {}", userId);

        Admin admin = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "userId", userId.toString()));

        return adminMapper.toResponse(admin);
    }

    @Override
    public Page<AdminResponse> getAllAdmins(Pageable pageable) {
        log.debug("Fetching all admins with pagination");

        return adminRepository.findAllWithUser(pageable)
                .map(adminMapper::toResponse);
    }

    @Override
    public List<AdminResponse> getAdminsByDepartment(String department) {
        log.debug("Fetching admins by department: {}", department);

        return adminRepository.findByDepartment(department).stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponse> getAdminsByAccessLevel(Integer accessLevel) {
        log.debug("Fetching admins by access level: {}", accessLevel);

        return adminRepository.findByAccessLevel(accessLevel).stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponse> getActiveAdmins() {
        log.debug("Fetching active admins");

        return adminRepository.findByIsActiveTrue().stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponse> getSuperAdmins() {
        log.debug("Fetching super admins");

        return adminRepository.findSuperAdmins().stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AdminResponse> searchAdmins(String keyword, Pageable pageable) {
        log.debug("Searching admins with keyword: {}", keyword);

        return adminRepository.searchAdmins(keyword, pageable)
                .map(adminMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteAdmin(UUID id) {
        log.info("Deleting admin profile with ID: {}", id);

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id.toString()));

        admin.setIsDeleted(true);
        adminRepository.save(admin);

        log.info("Successfully deleted admin profile with ID: {}", id);
    }

    @Override
    public boolean existsByAdminCode(String adminCode) {
        return adminRepository.existsByAdminCode(adminCode);
    }

    @Override
    public long countAdminsByDepartment(String department) {
        return adminRepository.countByDepartment(department);
    }

    @Override
    public long countActiveAdmins() {
        return adminRepository.countByIsActiveTrue();
    }
}

