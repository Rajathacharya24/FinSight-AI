package com.finsight.documentservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private Long documentId;
    private Instant uploadedAt;
    private int extractedChars;
    private String status;
}
