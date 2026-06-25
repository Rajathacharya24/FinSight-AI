package com.finsight.workflowservice.client;

import com.finsight.workflowservice.config.WorkflowProperties;
import com.finsight.workflowservice.dto.DocumentContentResponse;
import com.finsight.workflowservice.dto.DocumentUploadResponse;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.dto.ExtractionRequest;
import com.finsight.workflowservice.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocumentServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final WorkflowProperties properties;

    public DocumentUploadResponse uploadDocument(String title, String description, MultipartFile file) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("title", title);
            metadata.put("description", description != null ? description : "");

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> metadataPart = new HttpEntity<>(metadata, jsonHeaders);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart);
            body.add("file", filePart);

            DocumentUploadResponse response = restClientBuilder.build()
                    .post()
                    .uri(properties.getDocumentServiceUrl() + "/api/v1/documents")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(DocumentUploadResponse.class);

            if (response == null || response.getDocumentId() == null) {
                throw new WorkflowException("Document upload returned empty response");
            }
            return response;
        } catch (IOException | RestClientException e) {
            throw new WorkflowException("Failed to upload document", e);
        }
    }

    public String getDocumentContent(Long documentId) {
        try {
            DocumentContentResponse response = restClientBuilder.build()
                    .get()
                    .uri(properties.getDocumentServiceUrl() + "/api/v1/documents/{id}/content", documentId)
                    .retrieve()
                    .body(DocumentContentResponse.class);
            if (response == null || response.getContent() == null) {
                throw new WorkflowException("Document content not found for id: " + documentId);
            }
            return response.getContent();
        } catch (RestClientException e) {
            throw new WorkflowException("Failed to fetch document content", e);
        }
    }
}
