package org.erp.sms.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.enums.CertificateType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Certificate type is required")
    private CertificateType certificateType;

    private String remarks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicCertificateRequest {
        @NotBlank(message = "Enrollment number is required")
        private String enrollmentNumber;

        @NotNull(message = "Certificate type is required")
        private CertificateType certificateType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproveCertificateRequest {
        private String remarks;
        private String rejectionReason;
    }
}

