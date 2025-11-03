package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Find users with failed login attempts >= threshold
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold AND u.isActive = true")
    List<User> findUsersWithFailedLogins(@Param("threshold") Integer threshold);
}

