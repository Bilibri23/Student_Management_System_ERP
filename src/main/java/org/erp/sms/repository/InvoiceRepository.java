package org.erp.sms.repository;

import org.erp.sms.common.enums.InvoiceStatus;
import org.erp.sms.entity.Invoice;
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
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByStudentId(Long studentId);
    
    Page<Invoice> findByStudentId(Long studentId, Pageable pageable);
    
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.student.username = :enrollmentNumber " +
           "ORDER BY i.issueDate DESC")
    List<Invoice> findByEnrollmentNumber(@Param("enrollmentNumber") String enrollmentNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.student.username = :enrollmentNumber " +
           "AND i.status = :status ORDER BY i.issueDate DESC")
    List<Invoice> findByEnrollmentNumberAndStatus(
            @Param("enrollmentNumber") String enrollmentNumber,
            @Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = 'OVERDUE' OR " +
           "(i.status = 'PENDING' AND i.dueDate < :currentDate)")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status " +
           "AND i.issueDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalAmountByStatusAndDateRange(
            @Param("status") InvoiceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE " +
           "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.student.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.student.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.student.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CONCAT(i.student.firstName, ' ', i.student.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Invoice> searchInvoices(@Param("query") String query, Pageable pageable);
}

