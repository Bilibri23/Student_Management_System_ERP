package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;
import org.erp.sms.common.enums.CertificateStatus;
import org.erp.sms.common.enums.CertificateType;

import java.time.LocalDate;

@Entity
@Table(name = "certificates", indexes = {
    @Index(name = "idx_certificate_number", columnList = "certificateNumber", unique = true),
    @Index(name = "idx_student_certificate", columnList = "student_id, certificateType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.PENDING;

    @Column(unique = true)
    private String certificateNumber;

    @Column(nullable = false)
    private LocalDate requestedAt;

    private LocalDate issuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id")
    private User issuedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}

