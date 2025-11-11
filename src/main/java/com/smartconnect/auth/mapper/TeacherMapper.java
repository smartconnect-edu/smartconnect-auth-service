package com.smartconnect.auth.mapper;

import com.smartconnect.auth.dto.request.TeacherCreateRequest;
import com.smartconnect.auth.dto.request.TeacherUpdateRequest;
import com.smartconnect.auth.dto.response.TeacherResponse;
import com.smartconnect.auth.model.entity.Teacher;
import org.mapstruct.*;

/**
 * Mapper interface for Teacher entity and DTOs
 * Following DIP - Dependency Inversion Principle
 * Using MapStruct for type-safe mapping
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeacherMapper {

    /**
     * Map create request to entity
     */
    @Mapping(target = "user", ignore = true)  // Will be set by service
    Teacher toEntity(TeacherCreateRequest request);

    /**
     * Map entity to response
     * Including calculated fields
     */
    @Mapping(target = "yearsOfExperience", expression = "java(teacher.getYearsOfExperience())")
    @Mapping(target = "canTeach", expression = "java(teacher.canTeach())")
    @Mapping(target = "isSeniorTeacher", expression = "java(teacher.isSeniorTeacher())")
    @Mapping(target = "hasPhD", expression = "java(teacher.hasPhD())")
    @Mapping(target = "isProfessor", expression = "java(teacher.isProfessor())")
    TeacherResponse toResponse(Teacher teacher);

    /**
     * Update entity from update request
     * Null values are ignored for partial updates
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "teacherCode", ignore = true)  // Cannot update code
    void updateEntityFromRequest(TeacherUpdateRequest request, @MappingTarget Teacher teacher);
}

