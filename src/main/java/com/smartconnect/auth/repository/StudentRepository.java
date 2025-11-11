package com.smartconnect.auth.repository;

import com.smartconnect.auth.model.entity.Student;
import com.smartconnect.auth.model.enums.StudentStatus;
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
 * Repository interface for Student entity
 * Following ISP - Interface Segregation Principle (specific methods for student operations)
 * Following DIP - Dependency Inversion Principle (depend on abstraction)
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    /**
     * Find student by student code
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.user WHERE s.studentCode = :studentCode")
    Optional<Student> findByStudentCode(@Param("studentCode") String studentCode);

    /**
     * Find student by user ID
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findByUserId(UUID userId);
    
    /**
     * Find student by ID with user relationship fetched
     */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.user WHERE s.id = :id")
    Optional<Student> findByIdWithUser(@Param("id") UUID id);

    /**
     * Find students by major ID
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.majorId = :majorId")
    List<Student> findByMajorId(@Param("majorId") UUID majorId);

    /**
     * Find students by major ID with pagination
     */
    @Query(value = "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.majorId = :majorId",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Student s WHERE s.majorId = :majorId")
    Page<Student> findByMajorIdPaged(@Param("majorId") UUID majorId, Pageable pageable);

    /**
     * Find students by status
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.status = :status")
    List<Student> findByStatus(@Param("status") StudentStatus status);

    /**
     * Find students by admission year
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.admissionYear = :admissionYear")
    List<Student> findByAdmissionYear(@Param("admissionYear") Integer admissionYear);

    /**
     * Find students by major and admission year
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.majorId = :majorId AND s.admissionYear = :admissionYear")
    List<Student> findByMajorIdAndAdmissionYear(@Param("majorId") UUID majorId, @Param("admissionYear") Integer admissionYear);

    /**
     * Find students by status and admission year
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.status = :status AND s.admissionYear = :admissionYear")
    List<Student> findByStatusAndAdmissionYear(@Param("status") StudentStatus status, @Param("admissionYear") Integer admissionYear);

    /**
     * Check if student code exists
     */
    boolean existsByStudentCode(String studentCode);

    /**
     * Check if user ID already has a student profile
     */
    boolean existsByUserId(UUID userId);

    /**
     * Count students by major
     */
    long countByMajorId(UUID majorId);

    /**
     * Count students by status
     */
    long countByStatus(StudentStatus status);

    /**
     * Find honors students (GPA >= 3.5)
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.gpa >= 3.5 ORDER BY s.gpa DESC")
    List<Student> findHonorsStudents();

    /**
     * Find students with GPA in range
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.gpa BETWEEN :minGpa AND :maxGpa ORDER BY s.gpa DESC")
    List<Student> findStudentsByGpaRange(@Param("minGpa") Double minGpa, @Param("maxGpa") Double maxGpa);

    /**
     * Search students by name or student code
     */
    @Query(value = "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Student s JOIN s.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find active students by major
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user WHERE s.majorId = :majorId AND s.status = 'ACTIVE'")
    List<Student> findActiveStudentsByMajor(@Param("majorId") UUID majorId);

    /**
     * Find all students with user relationship fetched (for pagination)
     */
    @Query(value = "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Student s")
    Page<Student> findAllWithUser(Pageable pageable);
}

