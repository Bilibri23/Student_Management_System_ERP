package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResponse {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private ExamType examType;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private String location;
    private Double totalMarks;
    private String instructions;
    private List<InvigilatorInfo> invigilators;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvigilatorInfo {
        private Long id;
        private String fullName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamSchedule {
        private String semester;
        private String academicYear;
        private List<ExamResponse> exams;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdmitCard {
        private Long examId;
        private String examType;
        private LocalDate examDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String courseCode;
        private String courseName;
        private Long studentId;
        private String studentName;
        private String enrollmentNumber;
        private String instructions;
    }
}
