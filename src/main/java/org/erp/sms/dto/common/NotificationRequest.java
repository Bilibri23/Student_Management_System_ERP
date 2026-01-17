package org.erp.sms.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    @NotBlank(message = "Type is required")
    private String type; // INFO, SUCCESS, WARNING, ERROR, LEAVE_APPROVED, PAYMENT_RECEIVED, etc.

    private String actionUrl;

    private String referenceType; // INVOICE, LEAVE, PAYMENT, CERTIFICATE, etc.

    private Long referenceId;

    private String priority; // LOW, MEDIUM, HIGH
}

