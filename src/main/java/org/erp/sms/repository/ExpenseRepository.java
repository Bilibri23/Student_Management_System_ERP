package org.erp.sms.repository;

import org.erp.sms.common.enums.ExpenseCategory;
import org.erp.sms.common.enums.ExpenseStatus;
import org.erp.sms.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByRequestedById(Long requestedById);
    
    Page<Expense> findByRequestedById(Long requestedById, Pageable pageable);
    
    Page<Expense> findByStatus(ExpenseStatus status, Pageable pageable);
    
    Page<Expense> findByCategory(ExpenseCategory category, Pageable pageable);
    
    List<Expense> findByStatusAndRequestedDateBetween(
            ExpenseStatus status, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT e FROM Expense e WHERE e.status = 'PENDING' " +
           "ORDER BY e.requestedDate ASC")
    List<Expense> findPendingExpenses();
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.status = :status " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalAmountByStatusAndDateRange(
            @Param("status") ExpenseStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category " +
           "AND e.status = 'APPROVED' AND e.expenseDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalByCategoryAndDateRange(
            @Param("category") ExpenseCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.status = :status")
    long countByStatus(@Param("status") ExpenseStatus status);
}

