package com.finsight.ai.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Ad-hoc RAG query request — used by the "Ask about this application" UI feature.
 */
@Data
public class RagQueryRequest {

    private Long documentId;
    private Long workflowId;

    @NotBlank(message = "question must not be blank")
    private String question;
}
