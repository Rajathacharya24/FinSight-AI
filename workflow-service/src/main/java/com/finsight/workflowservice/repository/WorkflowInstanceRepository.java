package com.finsight.workflowservice.repository;

import com.finsight.workflowservice.model.WorkflowInstance;
import com.finsight.workflowservice.model.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    List<WorkflowInstance> findByUserId(Long userId);

    Page<WorkflowInstance> findByUserId(Long userId, Pageable pageable);

    Page<WorkflowInstance> findAll(Pageable pageable);

    Page<WorkflowInstance> findByStatus(WorkflowStatus status, Pageable pageable);

    Page<WorkflowInstance> findByUserIdAndStatus(Long userId, WorkflowStatus status, Pageable pageable);

    @Query("SELECT w FROM WorkflowInstance w WHERE " +
           "(:userId IS NULL OR w.userId = :userId) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:fromDate IS NULL OR w.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR w.createdAt <= :toDate)")
    Page<WorkflowInstance> findFiltered(
            @Param("userId") Long userId,
            @Param("status") WorkflowStatus status,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);
}
