package com.finsight.ai.service.controller;

import com.finsight.ai.service.dto.ExtractionRequest;
import com.finsight.ai.service.dto.ExtractionResponse;
import com.finsight.ai.service.dto.RagQueryRequest;
import com.finsight.ai.service.model.LoanApplicationExtraction;
import com.finsight.ai.service.repository.LoanApplicationExtractionRepository;
import com.finsight.ai.service.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "AI Extraction", description = "AI-powered loan application extraction and RAG query endpoints")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;
    private final LoanApplicationExtractionRepository repository;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /extract — internal endpoint called by workflow-service
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Extract loan application fields (internal)",
        description = "Runs the full RAG + reranking + LLM extraction pipeline. " +
                      "Called by workflow-service; not intended for direct consumer use."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Extraction succeeded"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Extraction pipeline error")
    })
    @PostMapping("/extract")
    public ResponseEntity<ExtractionResponse> extract(@Valid @RequestBody ExtractionRequest request) {
        ExtractionResponse response = aiService.extractAndPersist(
                request.getDocumentId(),
                request.getExtractedText(),
                request.getWorkflowId());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /extract/{workflowId} — retrieve stored extraction by workflow
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Get extraction result by workflow ID",
        description = "Returns the persisted extraction result for a given workflow."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Extraction result found"),
        @ApiResponse(responseCode = "404", description = "No extraction found for this workflowId")
    })
    @GetMapping("/extract/{workflowId}")
    public ResponseEntity<ExtractionResponse> getByWorkflowId(@PathVariable Long workflowId) {
        return repository.findFirstByWorkflowIdOrderByCreatedAtDesc(workflowId)
                .map(entity -> {
                    ExtractionResponse resp = new ExtractionResponse();
                    resp.setDocumentId(entity.getDocumentId());
                    resp.setWorkflowId(entity.getWorkflowId());
                    resp.setApplicantName(entity.getApplicantName());
                    resp.setIncome(entity.getIncome());
                    resp.setAddress(entity.getAddress());
                    resp.setLoanAmount(entity.getLoanAmount());
                    resp.setEmploymentStatus(entity.getEmploymentStatus());
                    resp.setExistingDebts(entity.getExistingDebts());
                    resp.setConfidenceScore(entity.getConfidenceScore() != null ? entity.getConfidenceScore() : 0.0);
                    resp.setExtractionDurationMs(entity.getExtractionDurationMs());
                    resp.setStatus("EXTRACTED");
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /rag/query — ad-hoc Q&A for "ask about this application" UI feature
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Ad-hoc RAG query",
        description = "Answers a free-form question about a document using vector similarity search + LLM. " +
                      "Powers the 'Ask about this application' feature in the UI."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Answer generated"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/rag/query")
    public ResponseEntity<Map<String, Object>> ragQuery(@Valid @RequestBody RagQueryRequest request) {
        long startMs = System.currentTimeMillis();
        log.info("RAG query: workflowId={} documentId={} question={}",
                request.getWorkflowId(), request.getDocumentId(), request.getQuestion());

        // Retrieve relevant chunks
        List<org.springframework.ai.document.Document> chunks = vectorStore.similaritySearch(
                SearchRequest.query(request.getQuestion()).withTopK(5));

        String context = chunks.stream()
                .map(org.springframework.ai.document.Document::getContent)
                .collect(Collectors.joining("\n\n"));

        // GUARDRAIL: question and retrieved context are in user message only
        String answer = chatClient.prompt()
                .system(s -> s.text(
                    "You are a helpful assistant answering questions about loan applications. " +
                    "Base your answer only on the provided context. " +
                    "If the answer is not in the context, say so clearly."))
                .user(u -> u.text(
                    "Context:\n{context}\n\nQuestion: {question}")
                    .param("context", context)
                    .param("question", request.getQuestion()))
                .call()
                .content();

        long durationMs = System.currentTimeMillis() - startMs;

        return ResponseEntity.ok(Map.of(
                "question", request.getQuestion(),
                "answer",   answer,
                "chunks",   chunks.size(),
                "durationMs", durationMs
        ));
    }
}
