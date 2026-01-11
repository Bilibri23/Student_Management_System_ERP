package org.erp.sms.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.LeaveType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Reason is required")
    private String reason;

    private String notes;

    private String contactDuringLeave;

    private Long coveringPersonId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproveLeaveRequest {
        private String approvalNotes;
        private String rejectionReason;
    }
}

