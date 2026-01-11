package org.erp.sms.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private String semester;
    private String academicYear;
    private String program; // Added from FeeStructure
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private String description;
    private String notes;
    private String feeBreakdown; // JSON string
    private List<FeeComponent> feeComponents; // Parsed fee breakdown
    private LocalDate paidDate;
    private Integer reminderCount;
    private LocalDate lastReminderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeeComponent {
        private String name;
        private BigDecimal amount;
    }
}

