package com.finsight.workflowservice.repository;

import com.finsight.workflowservice.model.WorkflowAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowAuditLogRepository extends JpaRepository<WorkflowAuditLog, Long> {

    List<WorkflowAuditLog> findByWorkflowIdOrderByCreatedAtAsc(Long workflowId);
}
