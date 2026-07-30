package com.finsight.workflowservice.service;

import com.finsight.workflowservice.dto.ReviewRequest;
import com.finsight.workflowservice.dto.WorkflowResponse;
import com.finsight.workflowservice.exception.WorkflowException;
import com.finsight.workflowservice.kafka.event.WorkflowEvent;
import com.finsight.workflowservice.kafka.producer.WorkflowEventProducer;
import com.finsight.workflowservice.model.*;
import com.finsight.workflowservice.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Handles human-in-the-loop review decisions.
 * A reviewer (ROLE_REVIEWER or ROLE_ADMIN) calls this after examining the extracted data
 * and low-confidence fields flagged by the AI service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AuditLogService auditLogService;
    private final WorkflowEventProducer eventProducer;

    /**
     * Records a review decision and transitions the workflow to the next state.
     *
     * @param workflowId  ID of the workflow to review
     * @param request     ReviewRequest with action (APPROVE/REJECT/REQUEST_INFO) and reason
     * @param reviewerUsername  username of the reviewer (from JWT X-User-Id header)
     * @return updated WorkflowResponse
     */
    @Transactional
    public WorkflowInstance submitReview(Long workflowId, ReviewRequest request, String reviewerUsername) {
        WorkflowInstance instance = workflowInstanceRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowException("Workflow not found: " + workflowId));

        // Only allow review on workflows in REVIEW_PENDING or MANUAL_REVIEW states
        if (instance.getStatus() != WorkflowStatus.REVIEW_PENDING
                && instance.getStatus() != WorkflowStatus.MANUAL_REVIEW) {
            throw new WorkflowException(
                    "Workflow " + workflowId + " is in state " + instance.getStatus() +
                    " and cannot be reviewed. Only REVIEW_PENDING or MANUAL_REVIEW workflows can be reviewed.");
        }

        WorkflowStatus newStatus = resolveNewStatus(request.getAction());

        // Record review metadata
        instance.setReviewAction(request.getAction());
        instance.setReviewReason(request.getReason());
        instance.setReviewedBy(reviewerUsername);
        instance.setReviewedAt(Instant.now());
        instance.setStatus(newStatus);
        workflowInstanceRepository.save(instance);

        // Immutable audit log entry
        auditLogService.logCompleted(
                workflowId,
                WorkflowStep.RECOMMEND,
                AgentType.DECISION_AGENT,
                "REVIEW_" + request.getAction(),
                Map.of(
                        "action", request.getAction(),
                        "reason", request.getReason(),
                        "reviewer", reviewerUsername,
                        "newStatus", newStatus.name()
                )
        );

        // Emit workflow status-changed event so notification-service can send emails
        eventProducer.publishAnalysisCompleted(
                workflowId,
                instance.getDocumentId(),
                Map.of(
                        "status", newStatus.name(),
                        "reviewAction", request.getAction(),
                        "reviewReason", request.getReason(),
                        "reviewedBy", reviewerUsername
                )
        );

        log.info("Workflow {} reviewed by {} → {} ({})", workflowId, reviewerUsername, newStatus, request.getAction());
        return instance;
    }

    private WorkflowStatus resolveNewStatus(String action) {
        return switch (action) {
            case "APPROVE"      -> WorkflowStatus.APPROVED;
            case "REJECT"       -> WorkflowStatus.REJECTED;
            case "REQUEST_INFO" -> WorkflowStatus.MANUAL_REVIEW;
            default             -> throw new WorkflowException("Unknown review action: " + action);
        };
    }
}
