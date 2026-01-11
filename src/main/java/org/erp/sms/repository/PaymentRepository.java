package org.erp.sms.repository;

import org.erp.sms.common.enums.PaymentStatus;
import org.erp.sms.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByInvoiceId(Long invoiceId);
    
    List<Payment> findByStudentId(Long studentId);
    
    Page<Payment> findByStudentId(Long studentId, Pageable pageable);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    Optional<Payment> findByReceiptNumber(String receiptNumber);
    
    @Query("SELECT p FROM Payment p WHERE p.student.username = :enrollmentNumber " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByEnrollmentNumber(@Param("enrollmentNumber") String enrollmentNumber);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalPaidAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.invoice.id = :invoiceId " +
           "AND p.status = 'COMPLETED'")
    java.math.BigDecimal calculateTotalPaidByInvoice(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    boolean existsByReceiptNumber(String receiptNumber);
}

