package com.finsight.workflowservice.repository;

import com.finsight.workflowservice.model.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
}
