package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.LeadSource;
import org.erp.sms.common.enums.LeadStatus;

import java.time.LocalDate;

@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_lead_status", columnList = "status"),
    @Index(name = "idx_lead_source", columnList = "source"),
    @Index(name = "idx_lead_email", columnList = "email"),
    @Index(name = "idx_lead_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead extends BaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource source;

    @Column(columnDefinition = "TEXT")
    private String inquiry; // What they're interested in

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDate inquiryDate;

    private LocalDate lastContactDate;

    private LocalDate conversionDate; // When converted to enrolled student

    // Assigned to (marketing staff member)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // Converted student (if converted)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_student_id")
    private User convertedStudent;

    // Campaign that generated this lead (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(nullable = false)
    @Builder.Default
    private Integer followUpCount = 0;

    private LocalDate nextFollowUpDate;

    // Program/Course interested in
    private String interestedProgram;

    // Expected enrollment date
    private LocalDate expectedEnrollmentDate;
}

