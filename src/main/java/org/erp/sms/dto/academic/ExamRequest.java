package org.erp.sms.dto.academic;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Exam type is required")
    private ExamType examType;

    @NotNull(message = "Exam date is required")
    @FutureOrPresent(message = "Exam date must be today or in the future")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    private Integer duration;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Total marks is required")
    @Min(value = 1, message = "Total marks must be at least 1")
    private Double totalMarks;

    private String instructions;

    private Set<Long> invigilatorIds;
}
