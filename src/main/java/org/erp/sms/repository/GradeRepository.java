package org.erp.sms.repository;

import org.erp.sms.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    Optional<Grade> findByStudentIdAndComponentId(Long studentId, Long componentId);
    
    @Query("SELECT AVG(g.marksObtained / gc.maxMarks * 100) FROM Grade g " +
           "JOIN g.component gc WHERE g.course.id = :courseId AND g.approved = true")
    Double calculateAveragePercentageByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId " +
           "AND g.approved = true ORDER BY g.course.academicYear DESC, g.course.semester DESC")
    List<Grade> findApprovedGradesByStudent(@Param("studentId") Long studentId);
}
