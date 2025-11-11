package com.smartconnect.auth.mapper;

import com.smartconnect.auth.dto.request.AdminCreateRequest;
import com.smartconnect.auth.dto.request.AdminUpdateRequest;
import com.smartconnect.auth.dto.response.AdminResponse;
import com.smartconnect.auth.model.entity.Admin;
import org.mapstruct.*;

/**
 * Mapper interface for Admin entity and DTOs
 * Following DIP - Dependency Inversion Principle
 * Using MapStruct for compile-time safe mapping
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AdminMapper {

    /**
     * Map create request to entity
     */
    @Mapping(target = "user", ignore = true)  // Will be set by service
    Admin toEntity(AdminCreateRequest request);

    /**
     * Map entity to response
     * Including calculated fields
     */
    @Mapping(target = "isSuperAdmin", expression = "java(admin.isSuperAdmin())")
    @Mapping(target = "hasFullAccess", expression = "java(admin.hasFullAccess())")
    AdminResponse toResponse(Admin admin);

    /**
     * Update entity from update request
     * Null values are ignored for partial updates
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "adminCode", ignore = true)  // Cannot update code
    void updateEntityFromRequest(AdminUpdateRequest request, @MappingTarget Admin admin);
}

