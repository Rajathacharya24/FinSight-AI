package com.finsight.ai.service.dto;

import lombok.Data;

@Data
public class ExtractionRequest {
    private Long documentId;
    private String extractedText;
}
