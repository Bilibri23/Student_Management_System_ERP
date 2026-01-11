package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.ExpenseCategory;
import org.erp.sms.common.enums.ExpenseStatus;
import org.erp.sms.common.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expense_category", columnList = "category"),
    @Index(name = "idx_expense_status", columnList = "status"),
    @Index(name = "idx_expense_requested_by", columnList = "requestedBy_id"),
    @Index(name = "idx_expense_approved_by", columnList = "approvedBy_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestedBy_id", nullable = false)
    private User requestedBy;

    @Column(nullable = false)
    private LocalDate requestedDate;

    // Approval workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedBy_id")
    private User approvedBy;

    private LocalDate approvedDate;

    @Column(columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Payment information (if expense has been paid)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentReference;

    private LocalDate paidDate;

    @Column(columnDefinition = "TEXT")
    private String vendorName; // Supplier/vendor name

    @Column(columnDefinition = "TEXT")
    private String vendorDetails; // Vendor contact info

    // Attachment/document reference (receipt, invoice, etc.)
    private String attachmentPath;
}

