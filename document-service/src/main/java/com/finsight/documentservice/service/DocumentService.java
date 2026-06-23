package com.finsight.documentservice.service;

import com.finsight.documentservice.config.DocumentStorageProperties;
import com.finsight.documentservice.dto.DocumentContentResponse;
import com.finsight.documentservice.dto.DocumentUploadRequest;
import com.finsight.documentservice.dto.DocumentUploadResponse;
import com.finsight.documentservice.model.Document;
import com.finsight.documentservice.model.DocumentContent;
import com.finsight.documentservice.repository.DocumentContentRepository;
import com.finsight.documentservice.repository.DocumentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final PdfExtractorService pdfExtractorService;
    private final DocumentStorageProperties storageProperties;

    /**
     * Handles the complete upload flow:
     *   1. Persist Document metadata (status = UPLOADED).
     *   2. Save the PDF file to the configured upload directory.
     *   3. Extract text using PdfExtractorService.
     *   4. Persist the extracted text in DocumentContent.
     *   5. Update Document status to EXTRACTED.
     *   6. Return a concise response.
     */
    public DocumentUploadResponse uploadDocument(@Valid DocumentUploadRequest request,
                                                 MultipartFile file) throws IOException {
        // 1️⃣ Save metadata record (status will be updated later)
        Document document = new Document();
        Long userId = request.getUserId();
        document.setUserId(userId != null ? userId : 0L);
        document.setFileName(file.getOriginalFilename());
        document.setStatus("UPLOADED");
        document.setUploadedAt(Instant.now());
        document = documentRepository.save(document);

        // 2️⃣ Store file on disk
        Path uploadDir = Path.of(storageProperties.getUploadDir());
        Files.createDirectories(uploadDir);
        String stored = document.getId() + "-" + file.getOriginalFilename();
        Path targetPath = uploadDir.resolve(stored);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 3️⃣ Extract text
        String extractedText = pdfExtractorService.extractText(targetPath.toFile());

        // 4️⃣ Persist extracted text
        DocumentContent content = new DocumentContent();
        content.setDocumentId(document.getId());
        content.setContent(extractedText);
        documentContentRepository.save(content);

        // 5️⃣ Update status
        document.setStatus("EXTRACTED");
        documentRepository.save(document);

        // 6️⃣ Build response
        DocumentUploadResponse response = new DocumentUploadResponse();
        response.setDocumentId(document.getId());
        response.setUploadedAt(document.getUploadedAt());
        response.setExtractedChars(extractedText != null ? extractedText.length() : 0);
        response.setStatus(document.getStatus());
        return response;
    }

    public DocumentContentResponse getDocumentContent(Long documentId) {
        DocumentContent content = documentContentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found for document: " + documentId));

        DocumentContentResponse response = new DocumentContentResponse();
        response.setDocumentId(documentId);
        response.setContent(content.getContent());
        return response;
    }
}
