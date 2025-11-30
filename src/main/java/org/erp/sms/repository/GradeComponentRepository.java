package org.erp.sms.repository;

import org.erp.sms.entity.GradeComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeComponentRepository extends JpaRepository<GradeComponent, Long> {
    List<GradeComponent> findByCourseId(Long courseId);
    
    @Query("SELECT SUM(gc.weightage) FROM GradeComponent gc WHERE gc.course.id = :courseId")
    Double sumWeightageByCourseId(@Param("courseId") Long courseId);
}
