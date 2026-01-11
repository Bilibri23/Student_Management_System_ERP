package org.erp.sms.dto.marketing;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.common.enums.CampaignType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 200, message = "Campaign name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Campaign type is required")
    private CampaignType type;

    private CampaignStatus status;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Budget format invalid")
    private BigDecimal budget;

    private String targetAudience;

    private Integer expectedLeads;

    private Integer expectedEnrollments;

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    private String metrics; // JSON string for campaign metrics

    private String notes;

    private String channel;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateCampaignStatusRequest {
        @NotNull(message = "Status is required")
        private CampaignStatus status;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateSpentAmountRequest {
        @NotNull(message = "Spent amount is required")
        @DecimalMin(value = "0.0", message = "Spent amount must be >= 0")
        private BigDecimal spentAmount;
        private String notes;
    }
}

