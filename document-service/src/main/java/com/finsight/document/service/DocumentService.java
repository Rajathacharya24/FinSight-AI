package com.finsight.document.service;

import com.finsight.document.domain.Document;
import com.finsight.document.domain.DocumentContent;
import com.finsight.document.event.DocumentUploadedEvent;
import com.finsight.document.repository.DocumentContentRepository;
import com.finsight.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final MinioService minioService;
    private final PdfExtractionService pdfExtractionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_DOCUMENT_UPLOADED = "document-uploaded-topic";

    public DocumentService(DocumentRepository documentRepository,
                           DocumentContentRepository documentContentRepository,
                           MinioService minioService,
                           PdfExtractionService pdfExtractionService,
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.documentRepository = documentRepository;
        this.documentContentRepository = documentContentRepository;
        this.minioService = minioService;
        this.pdfExtractionService = pdfExtractionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, String userId, String correlationId) {
        validatePdfFile(file);

        String documentId = UUID.randomUUID().toString();
        String objectName = documentId + "_" + file.getOriginalFilename();

        String storagePath;
        try {
            storagePath = minioService.uploadFile(objectName, file);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("MinIO upload failed: " + e.getMessage());
        }

        String extractedText = pdfExtractionService.extractTextFromPdf(file);

        Document document = Document.builder()
                .id(documentId)
                .userId(userId != null ? userId : "demo-user")
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storageLocation(storagePath)
                .status("PROCESSED")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        documentRepository.save(document);

        DocumentContent content = DocumentContent.builder()
                .documentId(documentId)
                .extractedText(extractedText)
                .charCount(extractedText.length())
                .wordCount(extractedText.isBlank() ? 0 : extractedText.split("\\s+").length)
                .extractedAt(ZonedDateTime.now())
                .build();

        documentContentRepository.save(content);

        // Publish Kafka Event
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .documentId(documentId)
                .userId(document.getUserId())
                .fileName(document.getFileName())
                .fileSizeBytes(document.getFileSize())
                .storageLocation(storagePath)
                .extractedText(extractedText)
                .timestamp(ZonedDateTime.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .build();

        try {
            kafkaTemplate.send(TOPIC_DOCUMENT_UPLOADED, documentId, event);
            log.info("Published DocumentUploadedEvent for document ID: {}", documentId);
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event for document ID {}: {}", documentId, e.getMessage());
        }

        return document;
    }

    public List<Document> getAllDocuments(String userId) {
        if (userId != null && !userId.isBlank()) {
            return documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return documentRepository.findAll();
    }

    public Document getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
    }

    public DocumentContent getDocumentContent(String documentId) {
        return documentContentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document content not found for ID: " + documentId));
    }

    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 15MB");
        }
    }
}
