package org.erp.sms.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.LeaveStatus;
import org.erp.sms.common.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private LeaveType leaveType;
    private LeaveStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private String notes;
    private LocalDate requestedDate;
    private Long approvedById;
    private String approvedByName;
    private LocalDate approvedDate;
    private String approvalNotes;
    private String rejectionReason;
    private String contactDuringLeave;
    private Long coveringPersonId;
    private String coveringPersonName;
    private Integer leaveBalanceBefore;
    private Integer leaveBalanceAfter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

