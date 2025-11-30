package org.erp.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.erp.sms.common.entity.BaseEntity;

@Entity
@Table(name = "grade_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeComponent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double weightage; // Percentage (0-100)

    @Column(nullable = false)
    private Double maxMarks;
}
