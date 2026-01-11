package org.erp.sms.dto.finance;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.ExpenseCategory;
import org.erp.sms.common.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount format invalid")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    private String vendorName;

    private String vendorDetails;

    private String attachmentPath;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproveExpenseRequest {
        private String approvalNotes;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarkAsPaidRequest {
        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        private String paymentReference;

        @NotNull(message = "Paid date is required")
        private LocalDate paidDate;
    }
}

