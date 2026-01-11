package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.LeaveStatus;
import org.erp.sms.common.enums.LeaveType;

import java.time.LocalDate;

@Entity
@Table(name = "leaves", indexes = {
    @Index(name = "idx_leave_employee", columnList = "employee_id"),
    @Index(name = "idx_leave_status", columnList = "status"),
    @Index(name = "idx_leave_type", columnList = "leaveType"),
    @Index(name = "idx_leave_dates", columnList = "startDate, endDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer totalDays; // Calculated from start and end date

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDate requestedDate;

    // Approval workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private LocalDate approvedDate;

    @Column(columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Employee contact during leave
    private String contactDuringLeave;

    // Covering person (colleague who will cover responsibilities)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "covering_person_id")
    private User coveringPerson;

    // Leave balance information (can be tracked separately)
    private Integer leaveBalanceBefore;
    private Integer leaveBalanceAfter;
}

