package com.smartconnect.auth.controller;

import com.smartconnect.auth.dto.request.AdminCreateRequest;
import com.smartconnect.auth.dto.request.AdminUpdateRequest;
import com.smartconnect.auth.dto.response.AdminResponse;
import com.smartconnect.auth.dto.response.ApiResponse;
import com.smartconnect.auth.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Admin operations
 * Only accessible by super admins
 */
@RestController
@RequestMapping("/v1/admins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "APIs for managing admin profiles (Super Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create admin profile", description = "Super Admin only")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(
            @Valid @RequestBody AdminCreateRequest request) {
        log.info("Creating admin profile for user ID: {}", request.getUserId());
        AdminResponse response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin profile created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @adminSecurity.isOwner(#id)")
    @Operation(summary = "Update admin profile")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateRequest request) {
        log.info("Updating admin profile with ID: {}", id);
        AdminResponse response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Admin profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @adminSecurity.isOwner(#id)")
    @Operation(summary = "Get admin by ID")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable UUID id) {
        AdminResponse response = adminService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{adminCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get admin by code")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminByCode(@PathVariable String adminCode) {
        AdminResponse response = adminService.getAdminByCode(adminCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @userSecurity.isOwner(#userId)")
    @Operation(summary = "Get admin by user ID")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminByUserId(@PathVariable UUID userId) {
        AdminResponse response = adminService.getAdminByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all admins")
    public ResponseEntity<ApiResponse<Page<AdminResponse>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AdminResponse> response = adminService.getAllAdmins(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get admins by department")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAdminsByDepartment(
            @PathVariable String department) {
        List<AdminResponse> response = adminService.getAdminsByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/access-level/{accessLevel}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get admins by access level")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAdminsByAccessLevel(
            @PathVariable Integer accessLevel) {
        List<AdminResponse> response = adminService.getAdminsByAccessLevel(accessLevel);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get active admins")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getActiveAdmins() {
        List<AdminResponse> response = adminService.getActiveAdmins();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/super-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get super admins")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getSuperAdmins() {
        List<AdminResponse> response = adminService.getSuperAdmins();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Search admins")
    public ResponseEntity<ApiResponse<Page<AdminResponse>>> searchAdmins(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminResponse> response = adminService.searchAdmins(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete admin profile")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable UUID id) {
        log.info("Deleting admin profile with ID: {}", id);
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Admin profile deleted successfully", null));
    }

    @GetMapping("/exists/code/{adminCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Check if admin code exists")
    public ResponseEntity<ApiResponse<Boolean>> existsByAdminCode(@PathVariable String adminCode) {
        boolean exists = adminService.existsByAdminCode(adminCode);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/count/department/{department}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Count admins by department")
    public ResponseEntity<ApiResponse<Long>> countAdminsByDepartment(@PathVariable String department) {
        long count = adminService.countAdminsByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Count active admins")
    public ResponseEntity<ApiResponse<Long>> countActiveAdmins() {
        long count = adminService.countActiveAdmins();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

