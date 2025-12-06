package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Long componentId;
    private String componentName;
    private Double weightage;
    private Double maxMarks;
    private Double marksObtained;
    private Double percentage;
    private String comments;
    private Boolean approved;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeComponentResponse {
        private Long id;
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String name;
        private String description;
        private Double weightage;
        private Double maxMarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseGradeSummary {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Integer credits;
        private List<ComponentGrade> componentGrades;
        private Double totalWeightedPercentage;
        private String letterGrade;
        private Double gradePoints;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComponentGrade {
        private String componentName;
        private Double weightage;
        private Double maxMarks;
        private Double marksObtained;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranscriptResponse {
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private List<SemesterGrades> semesters;
        private Double cumulativeGPA;
        private Integer totalCredits;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SemesterGrades {
            private String semester;
            private String academicYear;
            private List<CourseGradeSummary> courses;
            private Double semesterGPA;
            private Integer semesterCredits;
        }
    }
}
