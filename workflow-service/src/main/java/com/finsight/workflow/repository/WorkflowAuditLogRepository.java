package com.finsight.workflow.repository;

import com.finsight.workflow.domain.WorkflowAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowAuditLogRepository extends JpaRepository<WorkflowAuditLog, Long> {
    List<WorkflowAuditLog> findByWorkflowIdOrderByTimestampAsc(String workflowId);
}
