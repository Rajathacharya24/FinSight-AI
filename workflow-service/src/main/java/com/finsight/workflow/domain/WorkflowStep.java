package com.finsight.workflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String agentName;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String outputDetails;

    @Builder.Default
    private ZonedDateTime executedAt = ZonedDateTime.now();
}
