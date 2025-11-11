package com.smartconnect.auth.mapper;

import com.smartconnect.auth.dto.request.StudentCreateRequest;
import com.smartconnect.auth.dto.request.StudentUpdateRequest;
import com.smartconnect.auth.dto.response.StudentResponse;
import com.smartconnect.auth.model.entity.Student;
import org.mapstruct.*;

/**
 * Mapper interface for Student entity and DTOs
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 * Following SRP - Single responsibility for mapping
 * Using MapStruct for compile-time safe mapping
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface StudentMapper {

    /**
     * Map create request to entity
     * Following Factory Pattern
     */
    @Mapping(target = "user", ignore = true)  // Will be set by service
    Student toEntity(StudentCreateRequest request);

    /**
     * Map entity to response
     * Including calculated fields
     */
    @Mapping(target = "age", expression = "java(student.getAge())")
    @Mapping(target = "canEnroll", expression = "java(student.canEnroll())")
    @Mapping(target = "isHonorsStudent", expression = "java(student.isHonorsStudent())")
    StudentResponse toResponse(Student student);

    /**
     * Update entity from update request
     * Following OCP - Open for extension
     * Null values are ignored to support partial updates
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studentCode", ignore = true)  // Cannot update code
    @Mapping(target = "admissionYear", ignore = true)  // Cannot update admission year
    void updateEntityFromRequest(StudentUpdateRequest request, @MappingTarget Student student);
}

