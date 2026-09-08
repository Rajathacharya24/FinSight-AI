package com.finsight.ai.service;

import com.finsight.ai.domain.DocumentChunk;
import com.finsight.ai.dto.RagQueryRequest;
import com.finsight.ai.dto.RagQueryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorEmbeddingService vectorEmbeddingService;
    private final GroqLlmService groqLlmService;

    public RagService(VectorEmbeddingService vectorEmbeddingService, GroqLlmService groqLlmService) {
        this.vectorEmbeddingService = vectorEmbeddingService;
        this.groqLlmService = groqLlmService;
    }

    public RagQueryResponse queryDocument(String documentId, RagQueryRequest request) {
        List<DocumentChunk> relevantChunks = vectorEmbeddingService.retrieveRelevantChunks(documentId, request.getQuestion());

        String context = relevantChunks.stream()
                .map(DocumentChunk::getChunkText)
                .collect(Collectors.joining("\n---\n"));

        String answer = groqLlmService.generateRagAnswer(request.getQuestion(), context);

        return RagQueryResponse.builder()
                .documentId(documentId)
                .question(request.getQuestion())
                .answer(answer)
                .relevantSources(relevantChunks.stream().map(c -> "Chunk #" + c.getChunkIndex() + ": " + c.getChunkText()).toList())
                .build();
    }
}
