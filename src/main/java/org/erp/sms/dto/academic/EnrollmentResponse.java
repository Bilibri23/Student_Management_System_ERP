package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long id;
    private StudentInfo student;
    private CourseInfo course;
    private LocalDate enrollmentDate;
    private LocalDate dropDate;
    private Boolean approved;
    private Boolean active;
    private String notes;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentInfo {
        private Long id;
        private String username;
        private String fullName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseInfo {
        private Long id;
        private String courseCode;
        private String courseName;
        private Integer credits;
    }
}
