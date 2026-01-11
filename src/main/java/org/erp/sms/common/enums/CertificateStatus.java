package org.erp.sms.common.enums;

public enum CertificateStatus {
    PENDING,    // Request submitted, awaiting approval
    APPROVED,   // Request approved, certificate can be issued
    REJECTED,   // Request rejected
    ISSUED      // Certificate has been issued
}

