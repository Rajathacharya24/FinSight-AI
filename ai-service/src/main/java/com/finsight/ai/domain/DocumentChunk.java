package com.finsight.ai.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String chunkText;

    private ZonedDateTime createdAt = ZonedDateTime.now();

    public DocumentChunk() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String documentId;
        private Integer chunkIndex;
        private String chunkText;
        private ZonedDateTime createdAt = ZonedDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder chunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; return this; }
        public Builder chunkText(String chunkText) { this.chunkText = chunkText; return this; }
        public Builder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DocumentChunk build() {
            DocumentChunk c = new DocumentChunk();
            c.setId(id);
            c.setDocumentId(documentId);
            c.setChunkIndex(chunkIndex);
            c.setChunkText(chunkText);
            c.setCreatedAt(createdAt);
            return c;
        }
    }
}
