package org.erp.sms.dto.academic;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkAttendanceRequest {
        @NotNull(message = "Course ID is required")
        private Long courseId;

        @NotNull(message = "Session date is required")
        private LocalDate sessionDate;

        @NotNull(message = "Attendance records are required")
        private List<StudentAttendance> attendanceRecords;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAttendance {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Status is required")
        private AttendanceStatus status;

        private String notes;
    }
}
