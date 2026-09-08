package com.finsight.document.controller;

import com.finsight.document.domain.Document;
import com.finsight.document.domain.DocumentContent;
import com.finsight.document.service.DocumentService;
import com.finsight.document.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final MinioService minioService;

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        Document document = documentService.uploadDocument(file, userId, correlationId);
        return ResponseEntity.ok(document);
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(documentService.getAllDocuments(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable String id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<DocumentContent> getDocumentContent(@PathVariable String id) {
        return ResponseEntity.ok(documentService.getDocumentContent(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable String id) throws Exception {
        Document document = documentService.getDocumentById(id);
        String objectName = document.getStorageLocation().substring(document.getStorageLocation().indexOf("/") + 1);
        InputStream inputStream = minioService.getFileStream(objectName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }
}
