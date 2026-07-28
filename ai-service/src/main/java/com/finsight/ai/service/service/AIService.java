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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI extraction service with:
 * <ul>
 *   <li><strong>RAG pipeline</strong> — chunk → embed → retrieve (pgvector)</li>
 *   <li><strong>Reranking</strong> — a second LLM call scores each retrieved chunk 0-100 for
 *       relevance to the extraction goal; only the top-N are passed to the extraction prompt</li>
 *   <li><strong>Confidence scoring</strong> — final score [0, 100] derived from:
 *       number of chunks retrieved, reranking scores, and number of fields successfully parsed</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    // ─────────── Prompts ───────────

    private static final String RERANK_PROMPT = """
            You are a relevance scoring engine. Score the following text chunk 0-100 for how useful it is
            for extracting these loan application fields: applicant name, income, address, loan amount.
            Return ONLY a JSON object: {"score": <integer 0-100>}
            
            Chunk:
            {chunk}
            """;

    private static final String EXTRACTION_PROMPT = """
            Extract the following fields from this loan application text and return ONLY a JSON object.
            Do not include markdown code fences, explanations, or any text outside the JSON object.

            Fields:
            - applicantName: the full name of the applicant (string or null)
            - income: the annual income as digits only, e.g. "75000" (string or null)
            - address: the full address of the applicant (string or null)
            - loanAmount: the requested loan amount as digits only, e.g. "250000" (string or null)

            Text:
            {text}
            """;

    /** Number of chunks to retrieve from pgvector. */
    private static final int TOP_K_RETRIEVE = 8;
    /** Top chunks to pass to extraction after reranking. */
    private static final int TOP_K_RERANKED = 3;

    private final ChatClient chatClient;
    private final LoanApplicationExtractionRepository repository;
    private final ObjectMapper objectMapper;
    private final VectorStore vectorStore;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public ExtractionResponse extractAndPersist(Long documentId, String extractedText) {
        String text = extractedText == null ? "" : extractedText;
        log.info("Extracting from document {} ({} chars) using RAG + reranking pipeline", documentId, text.length());

        // ──── RAG: 1. Chunk & Embed ────
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> documents = splitter.split(List.of(new Document(text)));
        documents.forEach(doc -> doc.getMetadata().put("documentId", documentId));
        vectorStore.accept(documents);

        // ──── RAG: 2. Retrieve top-K chunks ────
        List<Document> retrieved = vectorStore.similaritySearch(
                SearchRequest.query("loan applicant name address income amount")
                             .withTopK(TOP_K_RETRIEVE)
                             .withFilterExpression("documentId == " + documentId));

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

        log.info("Reranked to top-{} chunks for documentId={}, avgScore={:.1f}",
                TOP_K_RERANKED, documentId, avgRerankScore);

        String ragText = reranked.stream()
                .map(sc -> sc.doc().getContent())
                .collect(Collectors.joining("\n\n"));

        // ──── RAG: 4. Extraction LLM call ────
        String response = chatClient.prompt()
                .system(s -> s.text("You are a loan application data extraction specialist. Return only valid JSON."))
                .user(u -> u.text(EXTRACTION_PROMPT).param("text", ragText))
                .call()
                .content();

        log.debug("LLM extraction response for documentId={}: {}", documentId, response);

        ExtractionResult result = parseResponse(response);

        // ──── Confidence Score ────
        double confidence = computeConfidence(retrieved.size(), avgRerankScore, result);
        log.info("Confidence score for documentId={}: {:.1f}", documentId, confidence);

        // ──── Persist ────
        LoanApplicationExtraction entity = new LoanApplicationExtraction();
        entity.setDocumentId(documentId != null ? documentId : 0L);
        entity.setApplicantName(result.applicantName());
        entity.setIncome(result.income());
        entity.setAddress(result.address());
        entity.setLoanAmount(result.loanAmount());
        entity.setConfidenceScore(confidence);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        ExtractionResponse extractionResponse = new ExtractionResponse();
        extractionResponse.setDocumentId(entity.getDocumentId());
        extractionResponse.setApplicantName(entity.getApplicantName());
        extractionResponse.setIncome(entity.getIncome());
        extractionResponse.setAddress(entity.getAddress());
        extractionResponse.setLoanAmount(entity.getLoanAmount());
        extractionResponse.setStatus("EXTRACTED");
        extractionResponse.setConfidenceScore(confidence);
        return extractionResponse;
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
     * Composite confidence score [0, 100]:
     * <ul>
     *   <li>40 pts — retrieval coverage (did we get chunks?)</li>
     *   <li>40 pts — average reranker score</li>
     *   <li>20 pts — extraction completeness (fields parsed / total fields)</li>
     * </ul>
     */
    private double computeConfidence(int retrievedCount, double avgRerankScore, ExtractionResult result) {
        // 40 pts retrieval: scale 0-5 chunks to 0-40
        double retrievalPts = Math.min(40.0, retrievedCount * 8.0);

        // 40 pts reranking (already 0-100; scale to 0-40)
        double rerankPts = avgRerankScore * 0.40;

        // 20 pts extraction completeness
        int fieldsFilled = 0;
        if (result.applicantName() != null && !result.applicantName().isBlank()) fieldsFilled++;
        if (result.income()         != null && !result.income().isBlank())         fieldsFilled++;
        if (result.address()        != null && !result.address().isBlank())        fieldsFilled++;
        if (result.loanAmount()     != null && !result.loanAmount().isBlank())     fieldsFilled++;
        double completenesspts = (fieldsFilled / 4.0) * 20.0;

        return Math.min(100.0, retrievalPts + rerankPts + completenesspts);
    }

    // ─────────────── Response parsing ───────────────

    private ExtractionResult parseResponse(String response) {
        String json = response
                .replaceAll("(?s)```\\w*\\s*", "")
                .replaceAll("(?s)```\\s*$", "")
                .trim();
        try {
            JsonNode root = objectMapper.readTree(json);
            return new ExtractionResult(
                    getText(root, "applicantName"),
                    getText(root, "income"),
                    getText(root, "address"),
                    getText(root, "loanAmount")
            );
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", response, e);
            return new ExtractionResult(null, null, null, null);
        }
    }

    private static String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    // ─────────────── Records ───────────────

    private record ExtractionResult(String applicantName, String income, String address, String loanAmount) {}
    private record ScoredChunk(Document doc, int score) {}
}
