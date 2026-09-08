package com.finsight.document.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "document_content")
public class DocumentContent {
    @Id
    private String documentId;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private Integer charCount;
    private Integer wordCount;

    private ZonedDateTime extractedAt = ZonedDateTime.now();

    public DocumentContent() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public Integer getCharCount() { return charCount; }
    public void setCharCount(Integer charCount) { this.charCount = charCount; }
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    public ZonedDateTime getExtractedAt() { return extractedAt; }
    public void setExtractedAt(ZonedDateTime extractedAt) { this.extractedAt = extractedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String documentId;
        private String extractedText;
        private Integer charCount;
        private Integer wordCount;
        private ZonedDateTime extractedAt = ZonedDateTime.now();

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder extractedText(String extractedText) { this.extractedText = extractedText; return this; }
        public Builder charCount(Integer charCount) { this.charCount = charCount; return this; }
        public Builder wordCount(Integer wordCount) { this.wordCount = wordCount; return this; }
        public Builder extractedAt(ZonedDateTime extractedAt) { this.extractedAt = extractedAt; return this; }

        public DocumentContent build() {
            DocumentContent c = new DocumentContent();
            c.setDocumentId(documentId);
            c.setExtractedText(extractedText);
            c.setCharCount(charCount);
            c.setWordCount(wordCount);
            c.setExtractedAt(extractedAt);
            return c;
        }
    }
}
