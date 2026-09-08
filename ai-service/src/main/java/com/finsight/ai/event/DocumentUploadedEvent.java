package com.finsight.ai.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadedEvent {
    private String documentId;
    private String userId;
    private String fileName;
    private Long fileSizeBytes;
    private String storageLocation;
    private String extractedText;
    private ZonedDateTime timestamp;
    private String correlationId;
}
