package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.erp.sms.common.entity.BaseEntity;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false, length = 50)
    private String type; // INFO, SUCCESS, WARNING, ERROR, LEAVE_APPROVED, PAYMENT_RECEIVED, etc.

    @Column(nullable = false)
    private Boolean read = false;

    @Column(length = 500)
    private String actionUrl; // URL to navigate when clicked

    @Column(length = 100)
    private String referenceType; // INVOICE, LEAVE, PAYMENT, CERTIFICATE, etc.

    @Column
    private Long referenceId; // ID of the referenced entity

    @Column(length = 100)
    private String priority; // LOW, MEDIUM, HIGH

    @Column
    private java.time.LocalDateTime readAt;

    // Helper method to get userId (for repository queries)
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}

