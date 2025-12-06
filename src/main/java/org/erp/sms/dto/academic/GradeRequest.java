package org.erp.sms.dto.academic;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Component ID is required")
    private Long componentId;

    @NotNull(message = "Marks obtained is required")
    @Min(value = 0, message = "Marks cannot be negative")
    private Double marksObtained;

    private String comments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkGradeRequest {
        @NotNull(message = "Component ID is required")
        private Long componentId;

        @NotNull(message = "Grades are required")
        private List<StudentGrade> grades;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentGrade {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Marks obtained is required")
        @Min(value = 0, message = "Marks cannot be negative")
        private Double marksObtained;

        private String comments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeComponentRequest {
        @NotNull(message = "Course ID is required")
        private Long courseId;

        @NotBlank(message = "Component name is required")
        private String name;

        private String description;

        @NotNull(message = "Weightage is required")
        @Min(value = 0, message = "Weightage cannot be negative")
        @Max(value = 100, message = "Weightage cannot exceed 100")
        private Double weightage;

        @NotNull(message = "Max marks is required")
        @Min(value = 0, message = "Max marks cannot be negative")
        private Double maxMarks;
    }
}
