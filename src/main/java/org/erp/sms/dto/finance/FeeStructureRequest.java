package org.erp.sms.dto.finance;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructureRequest {

    @NotBlank(message = "Program is required")
    private String program;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Fee name is required")
    private String name;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount format invalid")
    private BigDecimal amount;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isRequired = true;

    @Builder.Default
    private Boolean recurring = false;

    @Min(value = 1, message = "Due day must be between 1 and 31")
    @Max(value = 31, message = "Due day must be between 1 and 31")
    private Integer dueDayOfMonth;

    @Builder.Default
    private Integer orderIndex = 0;
}

