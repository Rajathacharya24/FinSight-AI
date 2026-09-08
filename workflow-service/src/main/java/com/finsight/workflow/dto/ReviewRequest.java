package com.finsight.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank
    private String action; // APPROVE or REJECT

    @NotBlank
    private String reason;
}
