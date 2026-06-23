package com.finsight.workflowservice.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class DocumentUploadResponse {

    private Long documentId;
    private Instant uploadedAt;
    private int extractedChars;
    private String status;
}
