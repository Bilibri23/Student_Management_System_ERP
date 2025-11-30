package org.erp.sms.repository;

import org.erp.sms.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentIdAndActiveTrue(Long studentId);
    List<Enrollment> findByCourseIdAndActiveTrue(Long courseId);
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);
    
    Optional<Enrollment> findByStudentIdAndCourseIdAndActiveTrue(Long studentId, Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.active = true")
    long countActiveEnrollmentsByStudentId(Long studentId);
    
    boolean existsByStudentIdAndCourseIdAndActiveTrue(Long studentId, Long courseId);
}
