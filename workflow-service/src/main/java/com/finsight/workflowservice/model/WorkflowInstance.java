package com.finsight.workflowservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "workflow_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "user_id", nullable = false)
    private Long userId = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WorkflowStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 50)
    private WorkflowStep currentStep;

    @Column(name = "extraction_data", columnDefinition = "TEXT")
    private String extractionData;

    @Column(name = "compliance_data", columnDefinition = "TEXT")
    private String complianceData;

    @Column(name = "decision_data", columnDefinition = "TEXT")
    private String decisionData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
