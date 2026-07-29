package com.finsight.workflowservice.controller;

import com.finsight.workflowservice.dto.*;
import com.finsight.workflowservice.model.WorkflowInstance;
import com.finsight.workflowservice.model.WorkflowStatus;
import com.finsight.workflowservice.service.AuditLogService;
import com.finsight.workflowservice.service.ReviewService;
import com.finsight.workflowservice.service.WorkflowOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Tag(name = "Workflows", description = "Loan application workflow management")
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WorkflowController {

    private final WorkflowOrchestrator workflowOrchestrator;
    private final ReviewService reviewService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST / — start a new workflow
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Start a new workflow",
        description = "Uploads a PDF document and starts the full loan processing pipeline."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Workflow started"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<WorkflowResponse> startWorkflow(
            @RequestPart("metadata") @Valid WorkflowStartRequest metadata,
            @RequestPart("file") MultipartFile file) {
        WorkflowResponse response = workflowOrchestrator.execute(
                metadata.getTitle(),
                metadata.getDescription(),
                file,
                metadata.getUserId());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /{id} — get a specific workflow
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get workflow by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Workflow found"),
        @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowOrchestrator.getWorkflow(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /{id}/audit-log — immutable audit trail
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get audit log for a workflow")
    @GetMapping("/{id}/audit-log")
    public ResponseEntity<List<AuditLogEntryDto>> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(workflowOrchestrator.getAuditLog(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /{id}/review — reviewer approves/rejects/requests info
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Submit review decision",
        description = "Reviewer (ROLE_REVIEWER or ROLE_ADMIN) approves, rejects, or requests more information. " +
                      "Only valid when workflow is in REVIEW_PENDING or MANUAL_REVIEW state."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid action or workflow not in reviewable state"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — REVIEWER or ADMIN required"),
        @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "unknown") String reviewerUsername) {

        WorkflowInstance updated = reviewService.submitReview(id, request, reviewerUsername);
        return ResponseEntity.ok(Map.of(
                "workflowId", updated.getId(),
                "status",     updated.getStatus().name(),
                "reviewAction", updated.getReviewAction(),
                "reviewedBy", updated.getReviewedBy(),
                "reviewedAt", updated.getReviewedAt().toString()
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET / — paginated list with filters
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "List workflows (paginated)",
        description = "Returns a paginated list of workflows. Supports filtering by status and date range."
    )
    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> listWorkflows() {
        return ResponseEntity.ok(workflowOrchestrator.listWorkflows());
    }
}
