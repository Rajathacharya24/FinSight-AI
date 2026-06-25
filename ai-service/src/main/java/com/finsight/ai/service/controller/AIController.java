package com.finsight.ai.service.controller;

import com.finsight.ai.service.dto.ExtractionRequest;
import com.finsight.ai.service.dto.ExtractionResponse;
import com.finsight.ai.service.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/extract")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<ExtractionResponse> extract(@Valid @RequestBody ExtractionRequest request,
                                                     @RequestParam(required = false) Long documentId) {
        Long resolvedDocId = documentId != null ? documentId : request.getDocumentId();
        ExtractionResponse response = aiService.extractAndPersist(resolvedDocId, request.getExtractedText());
        return ResponseEntity.ok(response);
    }
}
