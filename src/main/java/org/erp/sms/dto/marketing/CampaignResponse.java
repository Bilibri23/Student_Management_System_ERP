package org.erp.sms.dto.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.common.enums.CampaignType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private CampaignType type;
    private CampaignStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal spentAmount;
    private BigDecimal remainingBudget;
    private String targetAudience;
    private Integer expectedLeads;
    private Integer actualLeads;
    private Integer expectedEnrollments;
    private Integer actualEnrollments;
    private Long managerId;
    private String managerName;
    private String metrics; // JSON string
    private BigDecimal roi; // Return on Investment
    private String notes;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CampaignAnalytics {
        private Long campaignId;
        private String campaignName;
        private Integer totalLeads;
        private Integer convertedLeads;
        private Integer enrolledStudents;
        private BigDecimal costPerLead;
        private BigDecimal costPerEnrollment;
        private BigDecimal roi;
        private Integer totalImpressions;
        private Integer totalClicks;
        private BigDecimal clickThroughRate;
        private BigDecimal conversionRate;
    }
}

