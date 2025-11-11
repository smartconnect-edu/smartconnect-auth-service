package com.smartconnect.auth.service.impl;

import com.smartconnect.auth.dto.request.StudentCreateRequest;
import com.smartconnect.auth.dto.request.StudentUpdateRequest;
import com.smartconnect.auth.dto.response.StudentResponse;
import com.smartconnect.auth.exception.DuplicateResourceException;
import com.smartconnect.auth.exception.ProfileAlreadyExistsException;
import com.smartconnect.auth.exception.ResourceNotFoundException;
import com.smartconnect.auth.mapper.StudentMapper;
import com.smartconnect.auth.model.entity.Student;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.StudentStatus;
import com.smartconnect.auth.repository.StudentRepository;
import com.smartconnect.auth.repository.UserRepository;
import com.smartconnect.auth.service.StudentService;
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
 * Implementation of StudentService
 * Following SRP - Single Responsibility Principle (only student operations)
 * Following DIP - Depends on abstractions (interfaces)
 * Following OCP - Open for extension through interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        log.info("Creating student profile for user ID: {}", request.getUserId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        // Check if user already has a student profile
        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new ProfileAlreadyExistsException("Student", request.getUserId().toString());
        }

        // Check if student code already exists
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new DuplicateResourceException("Student", request.getStudentCode());
        }

        // Map and create student
        Student student = studentMapper.toEntity(request);
        student.setUser(user);

        Student savedStudent = studentRepository.save(student);
        log.info("Successfully created student profile with ID: {}", savedStudent.getId());

        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID id, StudentUpdateRequest request) {
        log.info("Updating student profile with ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id.toString()));

        // Update using mapper (null values ignored)
        studentMapper.updateEntityFromRequest(request, student);

        Student updatedStudent = studentRepository.save(student);
        log.info("Successfully updated student profile with ID: {}", id);

        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    public StudentResponse getStudentById(UUID id) {
        log.debug("Fetching student by ID: {}", id);

        Student student = studentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id.toString()));

        return studentMapper.toResponse(student);
    }

    @Override
    public StudentResponse getStudentByCode(String studentCode) {
        log.debug("Fetching student by code: {}", studentCode);

        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentCode", studentCode));

        return studentMapper.toResponse(student);
    }

    @Override
    public StudentResponse getStudentByUserId(UUID userId) {
        log.debug("Fetching student by user ID: {}", userId);

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId.toString()));

        return studentMapper.toResponse(student);
    }

    @Override
    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        log.debug("Fetching all students with pagination: {}", pageable);

        return studentRepository.findAllWithUser(pageable)
                .map(studentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudentsByMajor(UUID majorId, Pageable pageable) {
        log.debug("Fetching students by major ID: {} with pagination", majorId);

        return studentRepository.findByMajorIdPaged(majorId, pageable)
                .map(studentMapper::toResponse);
    }

    @Override
    public List<StudentResponse> getStudentsByStatus(StudentStatus status) {
        log.debug("Fetching students by status: {}", status);

        return studentRepository.findByStatus(status).stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByAdmissionYear(Integer admissionYear) {
        log.debug("Fetching students by admission year: {}", admissionYear);

        return studentRepository.findByAdmissionYear(admissionYear).stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getHonorsStudents() {
        log.debug("Fetching honors students (GPA >= 3.5)");

        return studentRepository.findHonorsStudents().stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentResponse> searchStudents(String keyword, Pageable pageable) {
        log.debug("Searching students with keyword: {}", keyword);

        return studentRepository.searchStudents(keyword, pageable)
                .map(studentMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteStudent(UUID id) {
        log.info("Deleting student profile with ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id.toString()));

        // Soft delete
        student.setIsDeleted(true);
        studentRepository.save(student);

        log.info("Successfully deleted student profile with ID: {}", id);
    }

    @Override
    public boolean existsByStudentCode(String studentCode) {
        return studentRepository.existsByStudentCode(studentCode);
    }

    @Override
    public long countStudentsByMajor(UUID majorId) {
        return studentRepository.countByMajorId(majorId);
    }

    @Override
    public long countStudentsByStatus(StudentStatus status) {
        return studentRepository.countByStatus(status);
    }
}

