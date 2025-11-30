package org.erp.sms.repository;

import org.erp.sms.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByCourseIdAndSessionDate(Long courseId, LocalDate sessionDate);
    Page<Attendance> findByCourseId(Long courseId, Pageable pageable);
    Page<Attendance> findByStudentId(Long studentId, Pageable pageable);
    
    Optional<Attendance> findByCourseIdAndStudentIdAndSessionDate(
            Long courseId, Long studentId, LocalDate sessionDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId " +
           "AND a.sessionDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByCourseIdAndDateRange(
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.sessionDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.course.id = :courseId AND a.status = 'PRESENT'")
    long countPresentByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.course.id = :courseId")
    long countTotalByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);
}
