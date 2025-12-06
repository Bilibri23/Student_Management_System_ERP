package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.CourseStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String department;
    private String semester;
    private String academicYear;
    private String description;
    private InstructorInfo instructor;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private Integer availableSeats;
    private String scheduleDay;
    private String startTime;
    private String endTime;
    private String room;
    private CourseStatus status;
    private String prerequisites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstructorInfo {
        private Long id;
        private String fullName;
        private String email;
    }
}
