package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_student", columnList = "student_id"),
    @Index(name = "idx_invoice_status", columnList = "status"),
    @Index(name = "idx_invoice_due_date", columnList = "dueDate"),
    @Index(name = "idx_invoice_number", columnList = "invoiceNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private String academicYear;

    @Column(nullable = false)
    private String program; // e.g., "Computer Science", "Business Administration"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Fee breakdown as JSON (store fee components)
    @Column(columnDefinition = "TEXT")
    private String feeBreakdown; // JSON: [{"name": "Tuition", "amount": 5000}, ...]

    private LocalDate paidDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer reminderCount = 0; // Number of reminders sent

    private LocalDate lastReminderDate;
}

