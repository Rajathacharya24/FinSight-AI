package com.finsight.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.ai.domain.LoanApplicationExtraction;
import com.finsight.ai.dto.ExtractionDto;
import com.finsight.ai.event.AIExtractionCompletedEvent;
import com.finsight.ai.event.DocumentUploadedEvent;
import com.finsight.ai.repository.LoanExtractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiDocumentListener {

    private final GroqLlmService groqLlmService;
    private final VectorEmbeddingService vectorEmbeddingService;
    private final LoanExtractionRepository extractionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_AI_COMPLETED = "ai-extraction-completed-topic";

    @KafkaListener(topics = "document-uploaded-topic", groupId = "ai-service-group")
    @Transactional
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Received DocumentUploadedEvent for document ID: {}", event.getDocumentId());

        try {
            // 1. Process structured extraction
            ExtractionDto extraction = groqLlmService.extractFinancialData(event.getExtractedText());

            LoanApplicationExtraction entity = LoanApplicationExtraction.builder()
                    .documentId(event.getDocumentId())
                    .borrowerName(extraction.getBorrowerName())
                    .loanAmount(extraction.getLoanAmount())
                    .annualIncome(extraction.getAnnualIncome())
                    .applicationDate(extraction.getApplicationDate())
                    .loanPurpose(extraction.getLoanPurpose())
                    .employmentStatus(extraction.getEmploymentStatus())
                    .confidenceScore(extraction.getConfidenceScore())
                    .rawExtractionJson(objectMapper.writeValueAsString(extraction))
                    .createdAt(ZonedDateTime.now())
                    .build();

            extractionRepository.save(entity);
            log.info("Saved extraction results for document ID: {}", event.getDocumentId());

            // 2. Vector chunking & embedding storage for RAG
            vectorEmbeddingService.processAndStoreDocumentChunks(event.getDocumentId(), event.getExtractedText());

            // 3. Publish completion event for workflow engine
            AIExtractionCompletedEvent completionEvent = AIExtractionCompletedEvent.builder()
                    .documentId(event.getDocumentId())
                    .userId(event.getUserId())
                    .borrowerName(extraction.getBorrowerName())
                    .loanAmount(extraction.getLoanAmount())
                    .annualIncome(extraction.getAnnualIncome())
                    .applicationDate(extraction.getApplicationDate())
                    .loanPurpose(extraction.getLoanPurpose())
                    .employmentStatus(extraction.getEmploymentStatus())
                    .confidenceScore(extraction.getConfidenceScore())
                    .timestamp(ZonedDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .build();

            kafkaTemplate.send(TOPIC_AI_COMPLETED, event.getDocumentId(), completionEvent);
            log.info("Published AIExtractionCompletedEvent for document ID: {}", event.getDocumentId());

        } catch (Exception e) {
            log.error("Error processing document uploaded event for document ID: {}", event.getDocumentId(), e);
        }
    }
}
