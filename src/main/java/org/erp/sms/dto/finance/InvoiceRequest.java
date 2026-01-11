package org.erp.sms.dto.finance;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Program is required")
    private String program;

    private String description;

    private String notes;

    private LocalDate dueDate; // If not provided, calculated from fee structure

    // Fee breakdown (optional - if not provided, calculated from FeeStructure)
    // Format: [{"name": "Tuition Fee", "amount": 5000}, {"name": "Lab Fee", "amount": 1000}]
    private String feeBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeeComponent {
        private String name;
        private BigDecimal amount;
    }
}

