package com.smartconnect.auth.mapper;

import com.smartconnect.auth.dto.response.UserResponse;
import com.smartconnect.auth.model.entity.User;
import org.mapstruct.Mapper;

/**
 * Mapper interface for User entity and DTOs
 * Following DIP - Dependency Inversion Principle
 * Used by other mappers for nested user information
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map entity to response
     * Excluding sensitive fields like password
     */
    UserResponse toResponse(User user);
}

