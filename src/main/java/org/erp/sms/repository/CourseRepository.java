package org.erp.sms.repository;

import org.erp.sms.common.enums.CourseStatus;
import org.erp.sms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);
    
    Page<Course> findByDepartment(String department, Pageable pageable);
    Page<Course> findBySemester(String semester, Pageable pageable);
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);
}
