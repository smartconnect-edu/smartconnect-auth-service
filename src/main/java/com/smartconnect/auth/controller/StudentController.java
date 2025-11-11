package com.smartconnect.auth.controller;

import com.smartconnect.auth.dto.request.StudentCreateRequest;
import com.smartconnect.auth.dto.request.StudentUpdateRequest;
import com.smartconnect.auth.dto.response.ApiResponse;
import com.smartconnect.auth.dto.response.StudentResponse;
import com.smartconnect.auth.model.enums.StudentStatus;
import com.smartconnect.auth.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * REST Controller for Student operations
 * Following REST best practices and SOLID principles
 */
@RestController
@RequestMapping("/v1/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Management", description = "APIs for managing student profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(
        summary = "Create student profile",
        description = "Create a new student profile for an existing user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Student profile created successfully",
            content = @Content(schema = @Schema(implementation = StudentResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Student profile already exists or student code is duplicate"
        )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentCreateRequest request) {
        log.info("Creating student profile for user ID: {}", request.getUserId());
        
        StudentResponse response = studentService.createStudent(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student profile created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF') or @studentSecurity.isOwner(#id)")
    @Operation(
        summary = "Update student profile",
        description = "Update an existing student profile"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Student profile updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Student not found"
        )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Parameter(description = "Student ID") @PathVariable UUID id,
            @Valid @RequestBody StudentUpdateRequest request) {
        log.info("Updating student profile with ID: {}", id);
        
        StudentResponse response = studentService.updateStudent(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Student profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER', 'SUPER_ADMIN') or @studentSecurity.isOwner(#id)")
    @Operation(
        summary = "Get student by ID",
        description = "Retrieve student profile by ID"
    )
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @Parameter(description = "Student ID") @PathVariable UUID id) {
        log.debug("Fetching student by ID: {}", id);
        
        StudentResponse response = studentService.getStudentById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get student by code",
        description = "Retrieve student profile by student code"
    )
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByCode(
            @Parameter(description = "Student code") @PathVariable String studentCode) {
        log.debug("Fetching student by code: {}", studentCode);
        
        StudentResponse response = studentService.getStudentByCode(studentCode);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER') or @userSecurity.isOwner(#userId)")
    @Operation(
        summary = "Get student by user ID",
        description = "Retrieve student profile by user ID"
    )
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.debug("Fetching student by user ID: {}", userId);
        
        StudentResponse response = studentService.getStudentByUserId(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get all students",
        description = "Retrieve all students with pagination"
    )
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getAllStudents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String direction) {
        log.debug("Fetching all students - page: {}, size: {}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StudentResponse> response = studentService.getAllStudents(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/major/{majorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get students by major",
        description = "Retrieve students by major ID with pagination"
    )
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudentsByMajor(
            @Parameter(description = "Major ID") @PathVariable UUID majorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Fetching students by major ID: {}", majorId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> response = studentService.getStudentsByMajor(majorId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get students by status",
        description = "Retrieve students by enrollment status"
    )
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByStatus(
            @Parameter(description = "Student status") @PathVariable StudentStatus status) {
        log.debug("Fetching students by status: {}", status);
        
        List<StudentResponse> response = studentService.getStudentsByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admission-year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get students by admission year",
        description = "Retrieve students by admission year"
    )
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByAdmissionYear(
            @Parameter(description = "Admission year") @PathVariable Integer year) {
        log.debug("Fetching students by admission year: {}", year);
        
        List<StudentResponse> response = studentService.getStudentsByAdmissionYear(year);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/honors")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Get honors students",
        description = "Retrieve students with GPA >= 3.5"
    )
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getHonorsStudents() {
        log.debug("Fetching honors students");
        
        List<StudentResponse> response = studentService.getHonorsStudents();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Search students",
        description = "Search students by name or student code"
    )
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> searchStudents(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Searching students with keyword: {}", keyword);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> response = studentService.searchStudents(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Delete student profile",
        description = "Soft delete a student profile (Admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Student profile deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Student not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "Student ID") @PathVariable UUID id) {
        log.info("Deleting student profile with ID: {}", id);
        
        studentService.deleteStudent(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>success("Student profile deleted successfully", null));
    }

    @GetMapping("/exists/code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(
        summary = "Check if student code exists",
        description = "Check if a student code is already in use"
    )
    public ResponseEntity<ApiResponse<Boolean>> existsByStudentCode(
            @Parameter(description = "Student code") @PathVariable String studentCode) {
        boolean exists = studentService.existsByStudentCode(studentCode);
        
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/count/major/{majorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(
        summary = "Count students by major",
        description = "Get the number of students in a major"
    )
    public ResponseEntity<ApiResponse<Long>> countStudentsByMajor(
            @Parameter(description = "Major ID") @PathVariable UUID majorId) {
        long count = studentService.countStudentsByMajor(majorId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'SUPER_ADMIN')")
    @Operation(
        summary = "Count students by status",
        description = "Get the number of students with a specific status"
    )
    public ResponseEntity<ApiResponse<Long>> countStudentsByStatus(
            @Parameter(description = "Student status") @PathVariable StudentStatus status) {
        long count = studentService.countStudentsByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

