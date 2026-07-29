package com.finsight.ai.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtractionRequest {

    @NotNull(message = "documentId is required")
    private Long documentId;

    /** Optional — links this extraction to a specific workflow for lookup by workflowId. */
    private Long workflowId;

    @NotBlank(message = "extractedText must not be blank")
    private String extractedText;
}
