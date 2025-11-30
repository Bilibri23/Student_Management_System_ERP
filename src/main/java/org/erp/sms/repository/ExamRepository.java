package org.erp.sms.repository;

import org.erp.sms.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseId(Long courseId);
    Page<Exam> findByExamDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    @Query("SELECT e FROM Exam e JOIN e.course c JOIN c.enrollments enr " +
           "WHERE enr.student.id = :studentId AND enr.active = true " +
           "ORDER BY e.examDate, e.startTime")
    List<Exam> findExamsByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Exam e WHERE :userId MEMBER OF e.invigilators")
    List<Exam> findExamsByInvigilatorId(@Param("userId") Long userId);
}
