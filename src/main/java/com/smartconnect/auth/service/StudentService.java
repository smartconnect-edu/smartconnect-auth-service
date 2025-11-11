package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.request.StudentCreateRequest;
import com.smartconnect.auth.dto.request.StudentUpdateRequest;
import com.smartconnect.auth.dto.response.StudentResponse;
import com.smartconnect.auth.model.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Student operations
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 * Following ISP - Interface Segregation Principle (specific methods)
 */
public interface StudentService {

    /**
     * Create a new student profile
     * @param request Student creation request
     * @return Created student response
     * @throws ProfileAlreadyExistsException if user already has student profile
     * @throws DuplicateResourceException if student code already exists
     * @throws ResourceNotFoundException if user not found
     */
    StudentResponse createStudent(StudentCreateRequest request);

    /**
     * Update existing student profile
     * @param id Student ID
     * @param request Student update request
     * @return Updated student response
     * @throws ResourceNotFoundException if student not found
     */
    StudentResponse updateStudent(UUID id, StudentUpdateRequest request);

    /**
     * Get student by ID
     * @param id Student ID
     * @return Student response
     * @throws ResourceNotFoundException if student not found
     */
    StudentResponse getStudentById(UUID id);

    /**
     * Get student by student code
     * @param studentCode Student code
     * @return Student response
     * @throws ResourceNotFoundException if student not found
     */
    StudentResponse getStudentByCode(String studentCode);

    /**
     * Get student by user ID
     * @param userId User ID
     * @return Student response
     * @throws ResourceNotFoundException if student not found
     */
    StudentResponse getStudentByUserId(UUID userId);

    /**
     * Get all students with pagination
     * @param pageable Pagination parameters
     * @return Page of student responses
     */
    Page<StudentResponse> getAllStudents(Pageable pageable);

    /**
     * Get students by major
     * @param majorId Major ID
     * @param pageable Pagination parameters
     * @return Page of student responses
     */
    Page<StudentResponse> getStudentsByMajor(UUID majorId, Pageable pageable);

    /**
     * Get students by status
     * @param status Student status
     * @return List of student responses
     */
    List<StudentResponse> getStudentsByStatus(StudentStatus status);

    /**
     * Get students by admission year
     * @param admissionYear Admission year
     * @return List of student responses
     */
    List<StudentResponse> getStudentsByAdmissionYear(Integer admissionYear);

    /**
     * Get honors students (GPA >= 3.5)
     * @return List of honors student responses
     */
    List<StudentResponse> getHonorsStudents();

    /**
     * Search students by keyword (name or student code)
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of student responses
     */
    Page<StudentResponse> searchStudents(String keyword, Pageable pageable);

    /**
     * Delete student profile (soft delete)
     * @param id Student ID
     * @throws ResourceNotFoundException if student not found
     */
    void deleteStudent(UUID id);

    /**
     * Check if student code exists
     * @param studentCode Student code
     * @return true if exists, false otherwise
     */
    boolean existsByStudentCode(String studentCode);

    /**
     * Count students by major
     * @param majorId Major ID
     * @return Number of students
     */
    long countStudentsByMajor(UUID majorId);

    /**
     * Count students by status
     * @param status Student status
     * @return Number of students
     */
    long countStudentsByStatus(StudentStatus status);
}

