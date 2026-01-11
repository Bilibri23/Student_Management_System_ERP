package org.erp.sms.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.PaymentMethod;
import org.erp.sms.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private String receiptNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String description;
    private String notes;
    private String transactionReference;
    private String gatewayResponse;
    private Long processedById;
    private String processedByName;
    private LocalDate processedDate;
    private String refundReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

