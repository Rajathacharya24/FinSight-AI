package com.finsight.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRequest {
    @NotBlank
    private String action;

    @NotBlank
    private String reason;

    public ReviewRequest() {}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
