package com.finsight.workflowservice.controller;

import com.finsight.workflowservice.dto.AuditLogEntryDto;
import com.finsight.workflowservice.dto.WorkflowResponse;
import com.finsight.workflowservice.dto.WorkflowStartRequest;
import com.finsight.workflowservice.service.WorkflowOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowOrchestrator workflowOrchestrator;

    /**
     * Starts the full loan processing workflow:
     * Upload → Extract → Validate → Recommend
     */
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

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowOrchestrator.getWorkflow(id));
    }

    @GetMapping("/{id}/audit-log")
    public ResponseEntity<List<AuditLogEntryDto>> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(workflowOrchestrator.getAuditLog(id));
    }

    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> listWorkflows() {
        return ResponseEntity.ok(workflowOrchestrator.listWorkflows());
    }
}
