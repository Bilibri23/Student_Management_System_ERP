package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long studentId;
    private String studentName;
    private LocalDate sessionDate;
    private AttendanceStatus status;
    private String markedByName;
    private String notes;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceSummary {
        private Long studentId;
        private String studentName;
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Long totalSessions;
        private Long presentCount;
        private Long absentCount;
        private Long lateCount;
        private Long excusedCount;
        private Double attendancePercentage;
    }
}
