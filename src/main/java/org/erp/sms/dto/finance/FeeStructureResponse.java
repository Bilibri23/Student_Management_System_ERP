package org.erp.sms.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructureResponse {
    private Long id;
    private String program;
    private String semester;
    private String academicYear;
    private String name;
    private String description;
    private BigDecimal amount;
    private Boolean isActive;
    private Boolean isRequired;
    private Boolean recurring;
    private Integer dueDayOfMonth;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

