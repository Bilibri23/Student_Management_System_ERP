package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_structures", indexes = {
    @Index(name = "idx_fee_structure_program", columnList = "program, semester, academicYear")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure extends BaseEntity {

    @Column(nullable = false)
    private String program; // e.g., "Computer Science", "Business Administration"

    @Column(nullable = false)
    private String semester; // e.g., "Fall 2024", "Spring 2025"

    @Column(nullable = false)
    private String academicYear; // e.g., "2024-2025"

    @Column(nullable = false)
    private String name; // e.g., "Tuition Fee", "Lab Fee", "Library Fee"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = true; // Required vs Optional fee

    @Column(nullable = false)
    @Builder.Default
    private Boolean recurring = false; // One-time or recurring (per semester/year)

    private Integer dueDayOfMonth; // Day of month when fee is due (1-31)

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0; // Display order
}

