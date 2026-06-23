package com.finsight.ai.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.ai.service.dto.ExtractionResponse;
import com.finsight.ai.service.model.LoanApplicationExtraction;
import com.finsight.ai.service.repository.LoanApplicationExtractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

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

    private final ChatClient chatClient;
    private final LoanApplicationExtractionRepository repository;
    private final ObjectMapper objectMapper;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public ExtractionResponse extractAndPersist(Long documentId, String extractedText) {
        String text = extractedText == null ? "" : extractedText;
        log.info("Extracting from document {} ({} chars) using LLM", documentId, text.length());

        String response = chatClient.prompt()
                .system(s -> s.text("You are a loan application data extraction specialist. Return only valid JSON."))
                .user(u -> u.text(EXTRACTION_PROMPT).param("text", text))
                .call()
                .content();

        log.debug("LLM response for document {}: {}", documentId, response);

        ExtractionResult result = parseResponse(response);

        LoanApplicationExtraction entity = new LoanApplicationExtraction();
        entity.setDocumentId(documentId != null ? documentId : 0L);
        entity.setApplicantName(result.applicantName());
        entity.setIncome(result.income());
        entity.setAddress(result.address());
        entity.setLoanAmount(result.loanAmount());
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        ExtractionResponse extractionResponse = new ExtractionResponse();
        extractionResponse.setDocumentId(entity.getDocumentId());
        extractionResponse.setApplicantName(entity.getApplicantName());
        extractionResponse.setIncome(entity.getIncome());
        extractionResponse.setAddress(entity.getAddress());
        extractionResponse.setLoanAmount(entity.getLoanAmount());
        extractionResponse.setStatus("EXTRACTED");
        return extractionResponse;
    }

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

    private record ExtractionResult(String applicantName, String income, String address, String loanAmount) {}
}
