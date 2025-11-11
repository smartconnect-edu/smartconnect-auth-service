package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getUserById(java.util.UUID id);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByEmail(String email);

    Page<UserResponse> getAllActiveUsers(Pageable pageable);

    UserResponse updateUserProfile(java.util.UUID id, String fullName, String phone, String avatarUrl);

    void deactivateUser(java.util.UUID id);

    void activateUser(java.util.UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

