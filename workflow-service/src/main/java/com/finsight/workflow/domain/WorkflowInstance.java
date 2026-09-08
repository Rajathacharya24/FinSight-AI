package com.finsight.workflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String documentId;

    @Column(nullable = false)
    private String userId;

    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;

    @Column(nullable = false)
    private String status; // PENDING, IN_PROGRESS, APPROVED, REJECTED, NEEDS_REVIEW

    private String decision; // APPROVED, REJECTED, NEEDS_REVIEW
    
    @Column(columnDefinition = "TEXT")
    private String decisionReason;

    @Builder.Default
    private ZonedDateTime startedAt = ZonedDateTime.now();

    private ZonedDateTime completedAt;

    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @OneToMany(mappedBy = "workflowId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();
}
