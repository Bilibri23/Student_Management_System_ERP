package org.erp.sms.dto.finance;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount format invalid")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String description;

    private String notes;

    private LocalDate paymentDate; // If not provided, uses current date

    private String transactionReference; // For online/bank payments

    private String gatewayResponse; // For online payments

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicPaymentRequest {
        @NotBlank(message = "Enrollment number is required")
        private String enrollmentNumber;

        @NotBlank(message = "Invoice number is required")
        private String invoiceNumber;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        private String transactionReference;
    }
}

