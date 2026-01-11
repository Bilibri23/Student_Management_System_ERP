package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.PaymentMethod;
import org.erp.sms.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
    @Index(name = "idx_payment_student", columnList = "student_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_receipt_number", columnList = "receiptNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Payment reference/transaction ID (for online payments, bank transfers)
    private String transactionReference;

    // Payment gateway response (for online payments)
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    // Processed by (finance staff)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private User processedBy;

    private LocalDate processedDate;

    @Column(columnDefinition = "TEXT")
    private String refundReason; // If payment is refunded
}

