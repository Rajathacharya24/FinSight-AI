package com.finsight.workflowservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for the human-in-the-loop review endpoint.
 * A reviewer (ROLE_REVIEWER or ROLE_ADMIN) submits this to advance
 * a workflow from REVIEW_PENDING to APPROVED / REJECTED / MANUAL_REVIEW.
 */
@Data
public class ReviewRequest {

    /**
     * The reviewer's decision.
     * Must be one of: APPROVE, REJECT, REQUEST_INFO
     */
    @NotBlank(message = "action is required")
    @Pattern(
        regexp = "^(APPROVE|REJECT|REQUEST_INFO)$",
        message = "action must be one of: APPROVE, REJECT, REQUEST_INFO"
    )
    private String action;

    /** Human-readable rationale for the decision (required). */
    @NotBlank(message = "reason is required")
    private String reason;
}
