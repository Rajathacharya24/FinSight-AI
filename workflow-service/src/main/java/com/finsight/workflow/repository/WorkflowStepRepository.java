package com.finsight.workflow.repository;

import com.finsight.workflow.domain.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByWorkflowIdOrderByExecutedAtAsc(String workflowId);
}
