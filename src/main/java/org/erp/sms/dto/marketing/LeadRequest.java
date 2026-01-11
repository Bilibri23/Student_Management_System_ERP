package org.erp.sms.dto.marketing;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.LeadSource;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String address;

    @NotNull(message = "Lead source is required")
    private LeadSource source;

    private String inquiry;

    private String notes;

    private LocalDate inquiryDate; // If not provided, uses current date

    private Long assignedToId;

    private Long campaignId; // Optional - link to campaign

    private String interestedProgram;

    private LocalDate expectedEnrollmentDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateLeadStatusRequest {
        private org.erp.sms.common.enums.LeadStatus status;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConvertToStudentRequest {
        @NotNull(message = "Student ID is required")
        private Long studentId;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleFollowUpRequest {
        @NotNull(message = "Follow-up date is required")
        private LocalDate nextFollowUpDate;
        private String notes;
    }
}

