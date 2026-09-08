package com.finsight.ai.controller;

import com.finsight.ai.domain.LoanApplicationExtraction;
import com.finsight.ai.dto.RagQueryRequest;
import com.finsight.ai.dto.RagQueryResponse;
import com.finsight.ai.repository.LoanExtractionRepository;
import com.finsight.ai.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final LoanExtractionRepository extractionRepository;
    private final RagService ragService;

    public AiController(LoanExtractionRepository extractionRepository, RagService ragService) {
        this.extractionRepository = extractionRepository;
        this.ragService = ragService;
    }

    @GetMapping("/extractions/{documentId}")
    public ResponseEntity<LoanApplicationExtraction> getExtraction(@PathVariable String documentId) {
        return extractionRepository.findByDocumentId(documentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/documents/{documentId}/query")
    public ResponseEntity<RagQueryResponse> queryDocument(
            @PathVariable String documentId,
            @Valid @RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragService.queryDocument(documentId, request));
    }
}
