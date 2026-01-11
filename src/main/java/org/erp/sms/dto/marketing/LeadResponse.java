package org.erp.sms.dto.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.LeadSource;
import org.erp.sms.common.enums.LeadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LeadStatus status;
    private LeadSource source;
    private String inquiry;
    private String notes;
    private LocalDate inquiryDate;
    private LocalDate lastContactDate;
    private LocalDate conversionDate;
    private Long assignedToId;
    private String assignedToName;
    private Long campaignId;
    private String campaignName;
    private Long convertedStudentId;
    private String convertedStudentName;
    private Integer followUpCount;
    private LocalDate nextFollowUpDate;
    private String interestedProgram;
    private LocalDate expectedEnrollmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

