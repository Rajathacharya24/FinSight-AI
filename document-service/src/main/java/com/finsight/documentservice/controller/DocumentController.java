package com.finsight.documentservice.controller;

import com.finsight.documentservice.dto.DocumentContentResponse;
import com.finsight.documentservice.dto.DocumentUploadRequest;
import com.finsight.documentservice.dto.DocumentUploadResponse;
import com.finsight.documentservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestPart("metadata") @Valid DocumentUploadRequest metadata,
            @RequestPart("file") MultipartFile file) throws IOException {
        DocumentUploadResponse response = documentService.uploadDocument(metadata, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDocument(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", id);
        response.put("status", "PROCESSED");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<DocumentContentResponse> getDocumentContent(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentContent(id));
    }
}
