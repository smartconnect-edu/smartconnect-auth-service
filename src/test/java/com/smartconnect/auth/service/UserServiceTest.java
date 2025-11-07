package com.smartconnect.auth.service;

import com.smartconnect.auth.dto.response.UserResponse;
import com.smartconnect.auth.exception.ResourceNotFoundException;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests user management operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .fullName("Test User")
                .phone("1234567890")
                .avatarUrl("https://example.com/avatar.jpg")
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(true)
                .lastLogin(LocalDateTime.now())
                .build();
        testUser.setId(testUserId);
    }

    // ==================== GET USER BY ID TESTS ====================

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(testUserId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUserId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getPhone()).isEqualTo("1234567890");
        assertThat(response.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getIsEmailVerified()).isTrue();

        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository).findById(nonExistentId);
    }

    // ==================== GET USER BY USERNAME TESTS ====================

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserByUsername("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username");

        verify(userRepository).findByUsername("nonexistent");
    }

    // ==================== GET USER BY EMAIL TESTS ====================

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmailSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // ==================== GET ALL ACTIVE USERS TESTS ====================

    @Test
    @DisplayName("Should get all active users successfully")
    void shouldGetAllActiveUsersSuccessfully() {
        // Given
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .role(UserRole.TEACHER)
                .isActive(true)
                .build();
        user2.setId(UUID.randomUUID());

        User user3 = User.builder()
                .username("user3")
                .email("user3@example.com")
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        user3.setId(UUID.randomUUID());

        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser, user2, user3));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAllByIsActiveTrue(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> response = userService.getAllActiveUsers(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent().get(0).getUsername()).isEqualTo("testuser");
        assertThat(response.getContent().get(1).getUsername()).isEqualTo("user2");
        assertThat(response.getContent().get(2).getUsername()).isEqualTo("user3");

        verify(userRepository).findAllByIsActiveTrue(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no active users")
    void shouldReturnEmptyPageWhenNoActiveUsers() {
        // Given
        Page<User> emptyPage = new PageImpl<>(Arrays.asList());
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAllByIsActiveTrue(pageable)).thenReturn(emptyPage);

        // When
        Page<UserResponse> response = userService.getAllActiveUsers(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();

        verify(userRepository).findAllByIsActiveTrue(pageable);
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    @DisplayName("Should update user profile successfully with all fields")
    void shouldUpdateUserProfileSuccessfullyWithAllFields() {
        // Given
        String newFullName = "Updated Name";
        String newPhone = "9876543210";
        String newAvatarUrl = "https://example.com/new-avatar.jpg";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.updateUserProfile(testUserId, newFullName, newPhone, newAvatarUrl);

        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getFullName()).isEqualTo(newFullName);
        assertThat(testUser.getPhone()).isEqualTo(newPhone);
        assertThat(testUser.getAvatarUrl()).isEqualTo(newAvatarUrl);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update only full name when other fields are null")
    void shouldUpdateOnlyFullNameWhenOtherFieldsAreNull() {
        // Given
        String originalPhone = testUser.getPhone();
        String originalAvatarUrl = testUser.getAvatarUrl();
        String newFullName = "Updated Name";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserProfile(testUserId, newFullName, null, null);

        // Then
        assertThat(testUser.getFullName()).isEqualTo(newFullName);
        assertThat(testUser.getPhone()).isEqualTo(originalPhone);
        assertThat(testUser.getAvatarUrl()).isEqualTo(originalAvatarUrl);

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update only phone when other fields are null")
    void shouldUpdateOnlyPhoneWhenOtherFieldsAreNull() {
        // Given
        String originalFullName = testUser.getFullName();
        String originalAvatarUrl = testUser.getAvatarUrl();
        String newPhone = "9876543210";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserProfile(testUserId, null, newPhone, null);

        // Then
        assertThat(testUser.getFullName()).isEqualTo(originalFullName);
        assertThat(testUser.getPhone()).isEqualTo(newPhone);
        assertThat(testUser.getAvatarUrl()).isEqualTo(originalAvatarUrl);

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update only avatar URL when other fields are null")
    void shouldUpdateOnlyAvatarUrlWhenOtherFieldsAreNull() {
        // Given
        String originalFullName = testUser.getFullName();
        String originalPhone = testUser.getPhone();
        String newAvatarUrl = "https://example.com/new-avatar.jpg";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserProfile(testUserId, null, null, newAvatarUrl);

        // Then
        assertThat(testUser.getFullName()).isEqualTo(originalFullName);
        assertThat(testUser.getPhone()).isEqualTo(originalPhone);
        assertThat(testUser.getAvatarUrl()).isEqualTo(newAvatarUrl);

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserProfile(nonExistentId, "Name", "Phone", "Avatar"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== DEACTIVATE USER TESTS ====================

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(testUserId);

        // Then
        assertThat(testUser.getIsActive()).isFalse();
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when deactivating non-existent user")
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deactivateUser(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should deactivate already inactive user")
    void shouldDeactivateAlreadyInactiveUser() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(testUserId);

        // Then
        assertThat(testUser.getIsActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    // ==================== ACTIVATE USER TESTS ====================

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(testUserId);

        // Then
        assertThat(testUser.getIsActive()).isTrue();
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when activating non-existent user")
    void shouldThrowExceptionWhenActivatingNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.activateUser(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should activate already active user")
    void shouldActivateAlreadyActiveUser() {
        // Given
        testUser.setIsActive(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(testUserId);

        // Then
        assertThat(testUser.getIsActive()).isTrue();
        verify(userRepository).save(testUser);
    }

    // ==================== EXISTS BY USERNAME TESTS ====================

    @Test
    @DisplayName("Should return true when username exists")
    void shouldReturnTrueWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean exists = userService.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean exists = userService.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
        verify(userRepository).existsByUsername("nonexistent");
    }

    // ==================== EXISTS BY EMAIL TESTS ====================

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean exists = userService.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean exists = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle user with null optional fields")
    void shouldHandleUserWithNullOptionalFields() {
        // Given
        testUser.setPhone(null);
        testUser.setAvatarUrl(null);
        testUser.setLastLogin(null);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(testUserId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPhone()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getLastLogin()).isNull();
    }

    @Test
    @DisplayName("Should handle pagination with different page sizes")
    void shouldHandlePaginationWithDifferentPageSizes() {
        // Given
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        Pageable pageable = PageRequest.of(0, 5);

        when(userRepository.findAllByIsActiveTrue(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> response = userService.getAllActiveUsers(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findAllByIsActiveTrue(pageable);
    }

    @Test
    @DisplayName("Should handle updating user with empty strings")
    void shouldHandleUpdatingUserWithEmptyStrings() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserProfile(testUserId, "", "", "");

        // Then
        assertThat(testUser.getFullName()).isEmpty();
        assertThat(testUser.getPhone()).isEmpty();
        assertThat(testUser.getAvatarUrl()).isEmpty();
        verify(userRepository).save(testUser);
    }
}

