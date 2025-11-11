package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.Teacher;
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
 * Repository interface for Teacher entity
 * Following ISP - Interface Segregation Principle (specific methods for teacher operations)
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    /**
     * Find teacher by teacher code
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.teacherCode = :teacherCode")
    Optional<Teacher> findByTeacherCode(@Param("teacherCode") String teacherCode);

    /**
     * Find teacher by user ID
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Teacher> findByUserId(UUID userId);

    /**
     * Find teachers by faculty ID
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.facultyId = :facultyId")
    List<Teacher> findByFacultyId(@Param("facultyId") UUID facultyId);

    /**
     * Find teachers by faculty ID with pagination
     */
    @Query(value = "SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.facultyId = :facultyId",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t WHERE t.facultyId = :facultyId")
    Page<Teacher> findByFacultyIdPaged(@Param("facultyId") UUID facultyId, Pageable pageable);

    /**
     * Find active teachers
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.isActive = true")
    List<Teacher> findByIsActiveTrue();

    /**
     * Find active teachers by faculty
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.facultyId = :facultyId AND t.isActive = true")
    List<Teacher> findByFacultyIdAndIsActiveTrue(@Param("facultyId") UUID facultyId);

    /**
     * Find teachers by title
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.title = :title")
    List<Teacher> findByTitle(@Param("title") String title);

    /**
     * Find teachers by degree
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE t.degree = :degree")
    List<Teacher> findByDegree(@Param("degree") String degree);

    /**
     * Check if teacher code exists
     */
    boolean existsByTeacherCode(String teacherCode);

    /**
     * Check if user ID already has a teacher profile
     */
    boolean existsByUserId(UUID userId);

    /**
     * Count teachers by faculty
     */
    long countByFacultyId(UUID facultyId);

    /**
     * Count active teachers
     */
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.isActive = true")
    long countByIsActiveTrue();

    /**
     * Find teachers with PhD degree
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE LOWER(t.degree) LIKE '%phd%' OR LOWER(t.degree) LIKE '%tiến sĩ%'")
    List<Teacher> findTeachersWithPhD();

    /**
     * Find professors (by title)
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE LOWER(t.title) LIKE '%professor%' OR LOWER(t.title) LIKE '%giáo sư%'")
    List<Teacher> findProfessors();

    /**
     * Search teachers by name, code, or specialization
     */
    @Query(value = "SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.teacherCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.specialization) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t JOIN t.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.teacherCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.specialization) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find senior teachers (>= 10 years experience)
     */
    @Query(value = "SELECT * FROM teachers t WHERE t.hire_date IS NOT NULL AND " +
           "EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM t.hire_date) >= 10", 
           nativeQuery = true)
    List<Teacher> findSeniorTeachers();

    /**
     * Find teachers by specialization
     */
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user WHERE LOWER(t.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    List<Teacher> findBySpecialization(@Param("specialization") String specialization);

    /**
     * Find all teachers with user relationship fetched (for pagination)
     */
    @Query(value = "SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.user",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t")
    Page<Teacher> findAllWithUser(Pageable pageable);
}

