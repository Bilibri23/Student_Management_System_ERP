package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.common.enums.CampaignType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "campaigns", indexes = {
    @Index(name = "idx_campaign_status", columnList = "status"),
    @Index(name = "idx_campaign_type", columnList = "type"),
    @Index(name = "idx_campaign_date", columnList = "startDate, endDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    // Target audience/segment
    @Column(columnDefinition = "TEXT")
    private String targetAudience;

    // Expected leads from this campaign
    private Integer expectedLeads;

    @Column(nullable = false)
    @Builder.Default
    private Integer actualLeads = 0;

    // Expected enrollments from this campaign
    private Integer expectedEnrollments;

    @Column(nullable = false)
    @Builder.Default
    private Integer actualEnrollments = 0;

    // Campaign manager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    // Campaign metrics
    @Column(columnDefinition = "TEXT")
    private String metrics; // JSON: impressions, clicks, conversions, etc.

    // Return on Investment (ROI)
    @Column(precision = 5, scale = 2)
    private BigDecimal roi;

    // Notes and observations
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Channel/platform (e.g., "Facebook Ads", "Google Ads", "Email Marketing")
    private String channel;
}

