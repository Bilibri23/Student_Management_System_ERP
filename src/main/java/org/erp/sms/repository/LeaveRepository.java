package org.erp.sms.repository;

import org.erp.sms.common.enums.LeaveStatus;
import org.erp.sms.common.enums.LeaveType;
import org.erp.sms.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    List<Leave> findByEmployeeId(Long employeeId);
    
    Page<Leave> findByEmployeeId(Long employeeId, Pageable pageable);
    
    Page<Leave> findByStatus(LeaveStatus status, Pageable pageable);
    
    Page<Leave> findByLeaveType(LeaveType leaveType, Pageable pageable);
    
    @Query("SELECT l FROM Leave l WHERE l.status = :status " +
           "AND l.startDate BETWEEN :startDate AND :endDate")
    List<Leave> findByStatusAndDateRange(
            @Param("status") LeaveStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.status = 'APPROVED' " +
           "AND ((l.startDate <= :date AND l.endDate >= :date) OR " +
           "(l.startDate BETWEEN :startDate AND :endDate) OR " +
           "(l.endDate BETWEEN :startDate AND :endDate))")
    List<Leave> findOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(l.totalDays) FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.leaveType = :leaveType AND l.status = 'APPROVED' " +
           "AND EXTRACT(YEAR FROM l.startDate) = :year")
    Integer calculateTotalLeaveDaysByTypeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("year") int year);
    
    @Query("SELECT l FROM Leave l WHERE l.status = 'PENDING' " +
           "ORDER BY l.requestedDate ASC")
    List<Leave> findPendingLeaves();
    
    @Query("SELECT l FROM Leave l WHERE l.approvedBy.id = :approverId " +
           "AND l.status = :status")
    List<Leave> findByApproverAndStatus(
            @Param("approverId") Long approverId,
            @Param("status") LeaveStatus status);
    
    @Query("SELECT COUNT(l) FROM Leave l WHERE l.status = :status")
    long countByStatus(@Param("status") LeaveStatus status);
    
    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.startDate <= :currentDate AND l.endDate >= :currentDate " +
           "AND l.status = 'APPROVED'")
    List<Leave> findCurrentLeaves(@Param("employeeId") Long employeeId, @Param("currentDate") LocalDate currentDate);
}

