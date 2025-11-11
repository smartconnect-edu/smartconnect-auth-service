package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Admin entity
 * Following ISP - Interface Segregation Principle (specific methods for admin operations)
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    /**
     * Find admin by admin code
     */
    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.adminCode = :adminCode")
    Optional<Admin> findByAdminCode(@Param("adminCode") String adminCode);

    /**
     * Find admin by user ID
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Admin> findByUserId(UUID userId);

    /**
     * Find admins by department
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.department = :department")
    List<Admin> findByDepartment(@Param("department") String department);

    /**
     * Find admins by department with pagination
     */
    @Query(value = "SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.department = :department",
           countQuery = "SELECT COUNT(DISTINCT a) FROM Admin a WHERE a.department = :department")
    Page<Admin> findByDepartment(@Param("department") String department, Pageable pageable);

    /**
     * Find active admins
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.isActive = true")
    List<Admin> findByIsActiveTrue();

    /**
     * Find admins by access level
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.accessLevel = :accessLevel")
    List<Admin> findByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * Find super admins (access level 3)
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.accessLevel = 3")
    List<Admin> findSuperAdmins();

    /**
     * Find admins with specific permission
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.canManageUsers = true")
    List<Admin> findAdminsWithUserManagementPermission();

    /**
     * Find admins with course management permission
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.canManageCourses = true")
    List<Admin> findAdminsWithCourseManagementPermission();

    /**
     * Find admins with grade management permission
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.canManageGrades = true")
    List<Admin> findAdminsWithGradeManagementPermission();

    /**
     * Find admins with system management permission
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.canManageSystem = true")
    List<Admin> findAdminsWithSystemManagementPermission();

    /**
     * Check if admin code exists
     */
    boolean existsByAdminCode(String adminCode);

    /**
     * Check if user ID already has an admin profile
     */
    boolean existsByUserId(UUID userId);

    /**
     * Count admins by department
     */
    long countByDepartment(String department);

    /**
     * Count active admins
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.isActive = true")
    long countByIsActiveTrue();

    /**
     * Count super admins
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.accessLevel = 3")
    long countSuperAdmins();

    /**
     * Search admins by name, code, or department
     */
    @Query(value = "SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.adminCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.department) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(DISTINCT a) FROM Admin a JOIN a.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.adminCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Admin> searchAdmins(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find active admins by department
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.department = :department AND a.isActive = true")
    List<Admin> findActiveAdminsByDepartment(@Param("department") String department);

    /**
     * Find admins with full access
     */
    @Query("SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user WHERE a.accessLevel = 3 OR " +
           "(a.canManageUsers = true AND a.canManageCourses = true AND " +
           "a.canManageGrades = true AND a.canManageSystem = true)")
    List<Admin> findAdminsWithFullAccess();

    /**
     * Find all admins with user relationship fetched (for pagination)
     */
    @Query(value = "SELECT DISTINCT a FROM Admin a LEFT JOIN FETCH a.user",
           countQuery = "SELECT COUNT(DISTINCT a) FROM Admin a")
    Page<Admin> findAllWithUser(Pageable pageable);
}

