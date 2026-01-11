package org.erp.sms.common.enums;

public enum LeaveStatus {
    PENDING,    // Request submitted, awaiting approval
    APPROVED,   // Leave approved
    REJECTED,   // Leave rejected
    CANCELLED,  // Leave cancelled by requester
    TAKEN       // Leave has been taken
}

