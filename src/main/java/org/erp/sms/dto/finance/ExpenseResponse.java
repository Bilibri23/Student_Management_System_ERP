package org.erp.sms.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.ExpenseCategory;
import org.erp.sms.common.enums.ExpenseStatus;
import org.erp.sms.common.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private ExpenseCategory category;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseStatus status;
    private Long requestedById;
    private String requestedByName;
    private LocalDate requestedDate;
    private Long approvedById;
    private String approvedByName;
    private LocalDate approvedDate;
    private String approvalNotes;
    private String rejectionReason;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDate paidDate;
    private String vendorName;
    private String vendorDetails;
    private String attachmentPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

