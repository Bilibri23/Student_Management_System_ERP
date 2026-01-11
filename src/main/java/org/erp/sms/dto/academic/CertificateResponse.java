package org.erp.sms.dto.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.CertificateStatus;
import org.erp.sms.common.enums.CertificateType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private String studentEmail;
    private CertificateType certificateType;
    private CertificateStatus status;
    private String certificateNumber;
    private LocalDate requestedAt;
    private LocalDate issuedAt;
    private Long issuedById;
    private String issuedByName;
    private String remarks;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CertificateDetails {
        private Long id;
        private String certificateNumber;
        private CertificateType certificateType;
        private String certificateTypeDisplay;
        private LocalDate issuedDate;
        private String studentName;
        private String enrollmentNumber;
        private String courseDetails;
        private String academicYear;
        private String remarks;
        private String issuedByName;
        private String schoolName;
        private String schoolAddress;
    }
}

