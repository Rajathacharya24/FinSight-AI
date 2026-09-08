package com.finsight.workflow.repository;

import com.finsight.workflow.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, String> {
    Optional<WorkflowInstance> findByDocumentId(String documentId);
    List<WorkflowInstance> findByUserIdOrderByStartedAtDesc(String userId);
}
