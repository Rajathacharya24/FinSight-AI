package com.finsight.ai.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.ai.service.dto.ExtractionResponse;
import com.finsight.ai.service.model.LoanApplicationExtraction;
import com.finsight.ai.service.repository.LoanApplicationExtractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI extraction service with:
 * <ul>
 *   <li><strong>RAG pipeline</strong> — chunk → embed → retrieve (pgvector)</li>
 *   <li><strong>Reranking</strong> — LLM scores each chunk 0-100; only top-N reach the extraction prompt</li>
 *   <li><strong>Schema validation + retry</strong> — validates JSON output; retries once with corrective prompt</li>
 *   <li><strong>Per-field confidence</strong> — individual scores per extracted field + aggregate</li>
 *   <li><strong>Guardrails</strong> — document text goes in user message only, never concatenated into system prompt</li>
 *   <li><strong>Cost/latency tracking</strong> — logs elapsed ms per call</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    // ─────────── Prompts ───────────

    private static final String RERANK_PROMPT = """
            You are a relevance scoring engine. Score the following text chunk 0-100 for how useful it is
            for extracting these loan application fields: applicant name, income, address, loan amount,
            employment status, existing debts.
            Return ONLY a JSON object: {"score": <integer 0-100>}
            
            Chunk:
            {chunk}
            """;

    /**
     * Extraction prompt — document text is always in the USER message, never the system prompt.
     * This prevents prompt injection from malicious document content overriding instructions.
     */
    private static final String EXTRACTION_PROMPT = """
            Extract the following fields from this loan application text and return ONLY a valid JSON object.
            Do not include markdown code fences, explanations, or any text outside the JSON object.
            For any field you cannot find, use null.

            Required fields:
            - applicantName: full name of the applicant (string or null)
            - income: annual income as digits only, e.g. "75000" (string or null)
            - address: full residential address (string or null)
            - loanAmount: requested loan amount as digits only, e.g. "250000" (string or null)
            - employmentStatus: e.g. "EMPLOYED", "SELF_EMPLOYED", "UNEMPLOYED", "RETIRED" (string or null)
            - existingDebts: total existing debt as digits only, e.g. "15000" (string or null)

            Text:
            {text}
            """;

    private static final String CORRECTIVE_PROMPT = """
            Your previous response was not valid JSON or was missing required fields.
            Return ONLY a valid JSON object with these fields (use null for any missing):
            {"applicantName": null, "income": null, "address": null, "loanAmount": null, "employmentStatus": null, "existingDebts": null}
            
            Previous malformed response:
            {previous}
            
            Loan application text:
            {text}
            """;

    /** Number of chunks to retrieve from pgvector. */
    private static final int TOP_K_RETRIEVE = 8;
    /** Top chunks to pass to extraction after reranking. */
    private static final int TOP_K_RERANKED = 3;

    // Required fields for schema validation
    private static final Set<String> REQUIRED_FIELDS = Set.of(
            "applicantName", "income", "address", "loanAmount", "employmentStatus", "existingDebts");

    private final ChatClient chatClient;
    private final LoanApplicationExtractionRepository repository;
    private final ObjectMapper objectMapper;
    private final VectorStore vectorStore;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public ExtractionResponse extractAndPersist(Long documentId, String extractedText, Long workflowId) {
        String text = extractedText == null ? "" : extractedText;
        long startMs = System.currentTimeMillis();
        log.info("Extracting from document {} ({} chars) workflowId={} using RAG + reranking pipeline",
                documentId, text.length(), workflowId);

        // ──── RAG: 1. Chunk & Embed ────
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> documents = splitter.split(List.of(new Document(text)));
        documents.forEach(doc -> doc.getMetadata().put("documentId", String.valueOf(documentId)));
        vectorStore.accept(documents);

        // ──── RAG: 2. Retrieve top-K chunks ────
        List<Document> retrieved = vectorStore.similaritySearch(
                SearchRequest.query("loan applicant name address income amount employment debts")
                             .withTopK(TOP_K_RETRIEVE));

        log.debug("Retrieved {} chunks for documentId={}", retrieved.size(), documentId);

        // ──── RAG: 3. Rerank — score each chunk with LLM ────
        List<ScoredChunk> reranked = retrieved.stream()
                .map(doc -> new ScoredChunk(doc, rerankChunk(doc.getContent())))
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(TOP_K_RERANKED)
                .toList();

        double avgRerankScore = reranked.stream()
                .mapToInt(ScoredChunk::score)
                .average()
                .orElse(0.0);

        log.info("Reranked to top-{} chunks for documentId={}, avgScore={}", TOP_K_RERANKED, documentId, String.format("%.1f", avgRerankScore));

        String ragText = reranked.stream()
                .map(sc -> sc.doc().getContent())
                .collect(Collectors.joining("\n\n"));

        // ──── RAG: 4. Extraction LLM call with schema validation + retry ────
        String response = chatClient.prompt()
                .system(s -> s.text(
                    // GUARDRAIL: system prompt contains only instructions, never user-provided text
                    "You are a loan application data extraction specialist. " +
                    "Return only valid JSON matching the exact schema requested. " +
                    "Never deviate from the schema. Never add commentary."))
                .user(u -> u.text(EXTRACTION_PROMPT).param("text", ragText))
                .call()
                .content();

        log.debug("LLM extraction response for documentId={}: {}", documentId, response);

        // ──── Schema validation + single retry ────
        ExtractionResult result = parseAndValidate(response);
        if (result == null) {
            log.warn("Schema validation failed for documentId={}, retrying with corrective prompt", documentId);
            String correctedResponse = chatClient.prompt()
                    .system(s -> s.text("You are a JSON repair specialist. Return only valid JSON."))
                    .user(u -> u.text(CORRECTIVE_PROMPT)
                                .param("previous", response)
                                .param("text", ragText))
                    .call()
                    .content();
            result = parseAndValidate(correctedResponse);
            if (result == null) {
                log.error("Corrective prompt also failed for documentId={}, using empty result", documentId);
                result = new ExtractionResult(null, null, null, null, null, null);
            }
        }

        // ──── Confidence Score (aggregate + per-field) ────
        Map<String, Integer> perFieldScores = computePerFieldConfidence(retrieved.size(), avgRerankScore, result);
        double aggregateConfidence = perFieldScores.values().stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);

        String fieldConfidenceJson;
        try {
            fieldConfidenceJson = objectMapper.writeValueAsString(perFieldScores);
        } catch (Exception e) {
            fieldConfidenceJson = "{}";
        }

        long durationMs = System.currentTimeMillis() - startMs;
        log.info("Extraction complete for documentId={} workflowId={} confidence={} durationMs={}",
                documentId, workflowId, String.format("%.1f", aggregateConfidence), durationMs);

        // ──── Persist ────
        LoanApplicationExtraction entity = new LoanApplicationExtraction();
        entity.setDocumentId(documentId != null ? documentId : 0L);
        entity.setWorkflowId(workflowId);
        entity.setApplicantName(result.applicantName());
        entity.setIncome(result.income());
        entity.setAddress(result.address());
        entity.setLoanAmount(result.loanAmount());
        entity.setEmploymentStatus(result.employmentStatus());
        entity.setExistingDebts(result.existingDebts());
        entity.setConfidenceScore(aggregateConfidence);
        entity.setFieldConfidenceScores(fieldConfidenceJson);
        entity.setExtractionDurationMs(durationMs);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        ExtractionResponse extractionResponse = new ExtractionResponse();
        extractionResponse.setDocumentId(entity.getDocumentId());
        extractionResponse.setWorkflowId(workflowId);
        extractionResponse.setApplicantName(entity.getApplicantName());
        extractionResponse.setIncome(entity.getIncome());
        extractionResponse.setAddress(entity.getAddress());
        extractionResponse.setLoanAmount(entity.getLoanAmount());
        extractionResponse.setEmploymentStatus(entity.getEmploymentStatus());
        extractionResponse.setExistingDebts(entity.getExistingDebts());
        extractionResponse.setStatus("EXTRACTED");
        extractionResponse.setConfidenceScore(aggregateConfidence);
        extractionResponse.setFieldConfidenceScores(perFieldScores);
        extractionResponse.setExtractionDurationMs(durationMs);
        return extractionResponse;
    }

    /** Convenience overload for callers that don't supply a workflowId. */
    public ExtractionResponse extractAndPersist(Long documentId, String extractedText) {
        return extractAndPersist(documentId, extractedText, null);
    }

    // ─────────────── Reranking ───────────────

    /**
     * Asks the LLM to score a single chunk for relevance.
     * Returns 0 on parse failure to ensure those chunks are ranked last.
     */
    private int rerankChunk(String chunkContent) {
        if (chunkContent == null || chunkContent.isBlank()) return 0;
        try {
            String raw = chatClient.prompt()
                    .user(u -> u.text(RERANK_PROMPT).param("chunk", chunkContent))
                    .call()
                    .content();
            String json = raw.replaceAll("(?s)```\\w*\\s*", "").replaceAll("(?s)```\\s*$", "").trim();
            JsonNode node = objectMapper.readTree(json);
            JsonNode scoreNode = node.get("score");
            return scoreNode != null ? Math.max(0, Math.min(100, scoreNode.asInt())) : 0;
        } catch (Exception e) {
            log.debug("Reranking failed for chunk ({}...), defaulting to 0: {}",
                    chunkContent.substring(0, Math.min(40, chunkContent.length())), e.getMessage());
            return 0;
        }
    }

    // ─────────────── Confidence Scoring ───────────────

    /**
     * Computes per-field confidence scores [0, 100] based on:
     * - Retrieval coverage (40 pts)
     * - Average reranker score (40 pts)
     * - Whether the field was actually parsed (20 pts, per field)
     */
    private Map<String, Integer> computePerFieldConfidence(int retrievedCount, double avgRerankScore, ExtractionResult result) {
        // Base score from retrieval quality (same for all fields)
        double retrievalPts = Math.min(40.0, retrievedCount * 8.0);
        double rerankPts    = avgRerankScore * 0.40;
        double baseScore    = retrievalPts + rerankPts;  // 0–80

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("applicantName",    fieldScore(baseScore, result.applicantName()));
        scores.put("income",           fieldScore(baseScore, result.income()));
        scores.put("address",          fieldScore(baseScore, result.address()));
        scores.put("loanAmount",       fieldScore(baseScore, result.loanAmount()));
        scores.put("employmentStatus", fieldScore(baseScore, result.employmentStatus()));
        scores.put("existingDebts",    fieldScore(baseScore, result.existingDebts()));
        return scores;
    }

    private int fieldScore(double baseScore, String value) {
        double fieldBonus = (value != null && !value.isBlank()) ? 20.0 : 0.0;
        return (int) Math.min(100.0, baseScore + fieldBonus);
    }

    // ─────────────── Response parsing / schema validation ───────────────

    /**
     * Parses LLM response, validates that it is a JSON object containing all required fields.
     * Returns null if validation fails (caller should retry with corrective prompt).
     */
    private ExtractionResult parseAndValidate(String response) {
        String json = response
                .replaceAll("(?s)```\\w*\\s*", "")
                .replaceAll("(?s)```\\s*$", "")
                .trim();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isObject()) {
                log.warn("LLM response is not a JSON object: {}", json.substring(0, Math.min(200, json.length())));
                return null;
            }
            // Validate all required keys exist (values can be null)
            for (String field : REQUIRED_FIELDS) {
                if (!root.has(field)) {
                    log.warn("LLM response missing required field '{}': {}", field, json.substring(0, Math.min(200, json.length())));
                    return null;
                }
            }
            return new ExtractionResult(
                    getText(root, "applicantName"),
                    getText(root, "income"),
                    getText(root, "address"),
                    getText(root, "loanAmount"),
                    getText(root, "employmentStatus"),
                    getText(root, "existingDebts")
            );
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as JSON: {}", e.getMessage());
            return null;
        }
    }

    private static String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    // ─────────────── Records ───────────────

    private record ExtractionResult(
            String applicantName,
            String income,
            String address,
            String loanAmount,
            String employmentStatus,
            String existingDebts
    ) {}

    private record ScoredChunk(Document doc, int score) {}
}
