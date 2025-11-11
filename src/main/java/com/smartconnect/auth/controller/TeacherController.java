package com.smartconnect.auth.controller;

import com.smartconnect.auth.dto.request.TeacherCreateRequest;
import com.smartconnect.auth.dto.request.TeacherUpdateRequest;
import com.smartconnect.auth.dto.response.ApiResponse;
import com.smartconnect.auth.dto.response.TeacherResponse;
import com.smartconnect.auth.service.TeacherService;
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
 * REST Controller for Teacher operations
 */
@RestController
@RequestMapping("/v1/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Management", description = "APIs for managing teacher profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Create teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(
            @Valid @RequestBody TeacherCreateRequest request) {
        log.info("Creating teacher profile for user ID: {}", request.getUserId());
        TeacherResponse response = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teacher profile created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF') or @teacherSecurity.isOwner(#id)")
    @Operation(summary = "Update teacher profile")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherUpdateRequest request) {
        log.info("Updating teacher profile with ID: {}", id);
        TeacherResponse response = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Teacher profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get teacher by ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable UUID id) {
        TeacherResponse response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{teacherCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get teacher by code")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByCode(@PathVariable String teacherCode) {
        TeacherResponse response = teacherService.getTeacherByCode(teacherCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER') or @userSecurity.isOwner(#userId)")
    @Operation(summary = "Get teacher by user ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByUserId(@PathVariable UUID userId) {
        TeacherResponse response = teacherService.getTeacherByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all teachers")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeacherResponse> response = teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/faculty/{facultyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get teachers by faculty")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> getTeachersByFaculty(
            @PathVariable UUID facultyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeacherResponse> response = teacherService.getTeachersByFaculty(facultyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get active teachers")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getActiveTeachers() {
        List<TeacherResponse> response = teacherService.getActiveTeachers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/senior")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Get senior teachers (>= 10 years experience)")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getSeniorTeachers() {
        List<TeacherResponse> response = teacherService.getSeniorTeachers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/phd")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Get teachers with PhD")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersWithPhD() {
        List<TeacherResponse> response = teacherService.getTeachersWithPhD();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Search teachers")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> searchTeachers(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeacherResponse> response = teacherService.searchTeachers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete teacher profile")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID id) {
        log.info("Deleting teacher profile with ID: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Teacher profile deleted successfully", null));
    }

    @GetMapping("/exists/code/{teacherCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Check if teacher code exists")
    public ResponseEntity<ApiResponse<Boolean>> existsByTeacherCode(@PathVariable String teacherCode) {
        boolean exists = teacherService.existsByTeacherCode(teacherCode);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/count/faculty/{facultyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Count teachers by faculty")
    public ResponseEntity<ApiResponse<Long>> countTeachersByFaculty(@PathVariable UUID facultyId) {
        long count = teacherService.countTeachersByFaculty(facultyId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Count active teachers")
    public ResponseEntity<ApiResponse<Long>> countActiveTeachers() {
        long count = teacherService.countActiveTeachers();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

