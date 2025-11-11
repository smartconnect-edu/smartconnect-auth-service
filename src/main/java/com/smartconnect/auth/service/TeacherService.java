package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.request.TeacherCreateRequest;
import com.smartconnect.auth.dto.request.TeacherUpdateRequest;
import com.smartconnect.auth.dto.response.TeacherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Teacher operations
 * Following DIP and ISP principles
 */
public interface TeacherService {

    /**
     * Create a new teacher profile
     */
    TeacherResponse createTeacher(TeacherCreateRequest request);

    /**
     * Update existing teacher profile
     */
    TeacherResponse updateTeacher(UUID id, TeacherUpdateRequest request);

    /**
     * Get teacher by ID
     */
    TeacherResponse getTeacherById(UUID id);

    /**
     * Get teacher by teacher code
     */
    TeacherResponse getTeacherByCode(String teacherCode);

    /**
     * Get teacher by user ID
     */
    TeacherResponse getTeacherByUserId(UUID userId);

    /**
     * Get all teachers with pagination
     */
    Page<TeacherResponse> getAllTeachers(Pageable pageable);

    /**
     * Get teachers by faculty
     */
    Page<TeacherResponse> getTeachersByFaculty(UUID facultyId, Pageable pageable);

    /**
     * Get active teachers
     */
    List<TeacherResponse> getActiveTeachers();

    /**
     * Get senior teachers (>= 10 years experience)
     */
    List<TeacherResponse> getSeniorTeachers();

    /**
     * Get teachers with PhD
     */
    List<TeacherResponse> getTeachersWithPhD();

    /**
     * Search teachers by keyword
     */
    Page<TeacherResponse> searchTeachers(String keyword, Pageable pageable);

    /**
     * Delete teacher profile (soft delete)
     */
    void deleteTeacher(UUID id);

    /**
     * Check if teacher code exists
     */
    boolean existsByTeacherCode(String teacherCode);

    /**
     * Count teachers by faculty
     */
    long countTeachersByFaculty(UUID facultyId);

    /**
     * Count active teachers
     */
    long countActiveTeachers();
}

