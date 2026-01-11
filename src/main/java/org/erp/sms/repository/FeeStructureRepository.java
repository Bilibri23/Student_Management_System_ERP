package org.erp.sms.repository;

import org.erp.sms.entity.FeeStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    
    List<FeeStructure> findByProgramAndSemesterAndAcademicYear(
            String program, String semester, String academicYear);
    
    List<FeeStructure> findByProgramAndAcademicYear(String program, String academicYear);
    
    List<FeeStructure> findByProgramAndIsActiveTrue(String program);
    
    Page<FeeStructure> findByIsActiveTrue(Pageable pageable);
    
    Page<FeeStructure> findByProgram(String program, Pageable pageable);
    
    @Query("SELECT fs FROM FeeStructure fs WHERE fs.program = :program " +
           "AND fs.semester = :semester AND fs.academicYear = :academicYear " +
           "AND fs.isActive = true ORDER BY fs.orderIndex ASC")
    List<FeeStructure> findActiveFeesByProgramSemesterYear(
            @Param("program") String program,
            @Param("semester") String semester,
            @Param("academicYear") String academicYear);
    
    @Query("SELECT SUM(fs.amount) FROM FeeStructure fs WHERE fs.program = :program " +
           "AND fs.semester = :semester AND fs.academicYear = :academicYear AND fs.isActive = true")
    java.math.BigDecimal calculateTotalFeeAmount(
            @Param("program") String program,
            @Param("semester") String semester,
            @Param("academicYear") String academicYear);
    
    boolean existsByProgramAndSemesterAndAcademicYearAndName(
            String program, String semester, String academicYear, String name);
}

