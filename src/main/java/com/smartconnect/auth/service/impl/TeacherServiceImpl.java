package com.smartconnect.auth.service.impl;

import com.smartconnect.auth.dto.request.TeacherCreateRequest;
import com.smartconnect.auth.dto.request.TeacherUpdateRequest;
import com.smartconnect.auth.dto.response.TeacherResponse;
import com.smartconnect.auth.exception.DuplicateResourceException;
import com.smartconnect.auth.exception.ProfileAlreadyExistsException;
import com.smartconnect.auth.exception.ResourceNotFoundException;
import com.smartconnect.auth.mapper.TeacherMapper;
import com.smartconnect.auth.model.entity.Teacher;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.repository.TeacherRepository;
import com.smartconnect.auth.repository.UserRepository;
import com.smartconnect.auth.service.TeacherService;
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
 * Implementation of TeacherService
 * Following SOLID principles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        log.info("Creating teacher profile for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId().toString()));

        if (teacherRepository.existsByUserId(request.getUserId())) {
            throw new ProfileAlreadyExistsException("Teacher", request.getUserId().toString());
        }

        if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new DuplicateResourceException("Teacher", request.getTeacherCode());
        }

        Teacher teacher = teacherMapper.toEntity(request);
        teacher.setUser(user);
        
        // Set default values if not provided
        if (teacher.getIsActive() == null) {
            teacher.setIsActive(true);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Successfully created teacher profile with ID: {}", savedTeacher.getId());

        return teacherMapper.toResponse(savedTeacher);
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(UUID id, TeacherUpdateRequest request) {
        log.info("Updating teacher profile with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id.toString()));

        teacherMapper.updateEntityFromRequest(request, teacher);

        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Successfully updated teacher profile with ID: {}", id);

        return teacherMapper.toResponse(updatedTeacher);
    }

    @Override
    public TeacherResponse getTeacherById(UUID id) {
        log.debug("Fetching teacher by ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id.toString()));

        return teacherMapper.toResponse(teacher);
    }

    @Override
    public TeacherResponse getTeacherByCode(String teacherCode) {
        log.debug("Fetching teacher by code: {}", teacherCode);

        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "teacherCode", teacherCode));

        return teacherMapper.toResponse(teacher);
    }

    @Override
    public TeacherResponse getTeacherByUserId(UUID userId) {
        log.debug("Fetching teacher by user ID: {}", userId);

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId.toString()));

        return teacherMapper.toResponse(teacher);
    }

    @Override
    public Page<TeacherResponse> getAllTeachers(Pageable pageable) {
        log.debug("Fetching all teachers with pagination");

        return teacherRepository.findAllWithUser(pageable)
                .map(teacherMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherResponse> getTeachersByFaculty(UUID facultyId, Pageable pageable) {
        log.debug("Fetching teachers by faculty ID: {}", facultyId);

        return teacherRepository.findByFacultyIdPaged(facultyId, pageable)
                .map(teacherMapper::toResponse);
    }

    @Override
    public List<TeacherResponse> getActiveTeachers() {
        log.debug("Fetching active teachers");

        return teacherRepository.findByIsActiveTrue().stream()
                .map(teacherMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponse> getSeniorTeachers() {
        log.debug("Fetching senior teachers");

        return teacherRepository.findSeniorTeachers().stream()
                .map(teacherMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherResponse> getTeachersWithPhD() {
        log.debug("Fetching teachers with PhD");

        return teacherRepository.findTeachersWithPhD().stream()
                .map(teacherMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TeacherResponse> searchTeachers(String keyword, Pageable pageable) {
        log.debug("Searching teachers with keyword: {}", keyword);

        return teacherRepository.searchTeachers(keyword, pageable)
                .map(teacherMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTeacher(UUID id) {
        log.info("Deleting teacher profile with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id.toString()));

        teacher.setIsDeleted(true);
        teacherRepository.save(teacher);

        log.info("Successfully deleted teacher profile with ID: {}", id);
    }

    @Override
    public boolean existsByTeacherCode(String teacherCode) {
        return teacherRepository.existsByTeacherCode(teacherCode);
    }

    @Override
    public long countTeachersByFaculty(UUID facultyId) {
        return teacherRepository.countByFacultyId(facultyId);
    }

    @Override
    public long countActiveTeachers() {
        return teacherRepository.countByIsActiveTrue();
    }
}

