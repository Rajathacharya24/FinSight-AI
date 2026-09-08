package com.finsight.document.event;

import java.time.ZonedDateTime;

public class DocumentUploadedEvent {
    private String documentId;
    private String userId;
    private String fileName;
    private Long fileSizeBytes;
    private String storageLocation;
    private String extractedText;
    private ZonedDateTime timestamp;
    private String correlationId;

    public DocumentUploadedEvent() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String documentId;
        private String userId;
        private String fileName;
        private Long fileSizeBytes;
        private String storageLocation;
        private String extractedText;
        private ZonedDateTime timestamp;
        private String correlationId;

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder fileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public Builder storageLocation(String storageLocation) { this.storageLocation = storageLocation; return this; }
        public Builder extractedText(String extractedText) { this.extractedText = extractedText; return this; }
        public Builder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

        public DocumentUploadedEvent build() {
            DocumentUploadedEvent e = new DocumentUploadedEvent();
            e.setDocumentId(documentId);
            e.setUserId(userId);
            e.setFileName(fileName);
            e.setFileSizeBytes(fileSizeBytes);
            e.setStorageLocation(storageLocation);
            e.setExtractedText(extractedText);
            e.setTimestamp(timestamp);
            e.setCorrelationId(correlationId);
            return e;
        }
    }
}
