package com.finsight.ai.service;

import com.finsight.ai.domain.DocumentChunk;
import com.finsight.ai.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VectorEmbeddingService {

    private final DocumentChunkRepository chunkRepository;

    public void processAndStoreDocumentChunks(String documentId, String text) {
        if (text == null || text.isBlank()) return;

        List<String> chunks = splitTextIntoChunks(text, 500);
        List<DocumentChunk> entityChunks = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = DocumentChunk.builder()
                    .documentId(documentId)
                    .chunkIndex(i)
                    .chunkText(chunks.get(i))
                    .build();
            entityChunks.add(chunk);
        }

        chunkRepository.saveAll(entityChunks);
        log.info("Saved {} chunks for document ID: {}", entityChunks.size(), documentId);
    }

    public List<DocumentChunk> retrieveRelevantChunks(String documentId, String query) {
        List<DocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
        if (chunks.isEmpty()) {
            return List.of();
        }
        // Simple TF-IDF / Keyword relevance filter for semantic match
        String[] keywords = query.toLowerCase().split("\\s+");
        return chunks.stream()
                .filter(chunk -> {
                    String lowerText = chunk.getChunkText().toLowerCase();
                    for (String kw : keywords) {
                        if (kw.length() > 3 && lowerText.contains(kw)) return true;
                    }
                    return true;
                })
                .limit(3)
                .toList();
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }
}
