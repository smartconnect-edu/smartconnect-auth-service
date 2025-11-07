package com.smartconnect.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconnect.auth.config.TestConfig;
import com.smartconnect.auth.dto.response.UserResponse;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 * Tests user management endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        userResponse = UserResponse.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== GET USER BY ID TESTS ====================

    @Test
    @WithMockUser
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Given
        when(userService.getUserById(testUserId)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserById(testUserId);
    }

    // ==================== GET USER BY USERNAME TESTS ====================

    @Test
    @WithMockUser
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/users/username/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserByUsername("testuser");
    }

    // ==================== GET ALL ACTIVE USERS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all active users successfully")
    void shouldGetAllActiveUsersSuccessfully() throws Exception {
        // Given
        UserResponse user2 = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .email("user2@example.com")
                .role(UserRole.TEACHER)
                .isActive(true)
                .build();

        Page<UserResponse> userPage = new PageImpl<>(Arrays.asList(userResponse, user2));
        when(userService.getAllActiveUsers(any(PageRequest.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(userService).getAllActiveUsers(any(PageRequest.class));
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    @WithMockUser
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfileSuccessfully() throws Exception {
        // Given
        String requestBody = "{\"fullName\":\"Updated Name\",\"phone\":\"9876543210\",\"avatarUrl\":\"https://example.com/avatar.jpg\"}";
        
        when(userService.updateUserProfile(eq(testUserId), anyString(), anyString(), anyString()))
                .thenReturn(userResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", testUserId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User profile updated successfully"));

        verify(userService).updateUserProfile(eq(testUserId), anyString(), anyString(), anyString());
    }

    // ==================== DEACTIVATE USER TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser(testUserId);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", testUserId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));

        verify(userService).deactivateUser(testUserId);
    }

    // ==================== ACTIVATE USER TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).activateUser(testUserId);

        // When & Then
        mockMvc.perform(post("/api/v1/users/{id}/activate", testUserId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User activated successfully"));

        verify(userService).activateUser(testUserId);
    }
}

