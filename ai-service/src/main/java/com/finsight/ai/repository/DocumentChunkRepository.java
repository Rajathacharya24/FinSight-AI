package com.finsight.ai.repository;

import com.finsight.ai.domain.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(String documentId);
}
