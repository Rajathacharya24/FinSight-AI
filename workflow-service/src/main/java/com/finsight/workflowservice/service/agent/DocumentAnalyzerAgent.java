package com.finsight.workflowservice.service.agent;

import com.finsight.workflowservice.client.AIServiceClient;
import com.finsight.workflowservice.client.DocumentServiceClient;
import com.finsight.workflowservice.dto.DocumentUploadResponse;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.model.AgentType;
import com.finsight.workflowservice.model.WorkflowStep;
import com.finsight.workflowservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocumentAnalyzerAgent {

    private final DocumentServiceClient documentServiceClient;
    private final AIServiceClient aiServiceClient;
    private final AuditLogService auditLogService;

    public DocumentUploadResponse upload(Long workflowId, String title, String description, MultipartFile file) {
        auditLogService.logStarted(workflowId, WorkflowStep.UPLOAD, AgentType.DOCUMENT_ANALYZER,
                "upload_document", Map.of("title", title, "fileName", file.getOriginalFilename()));

        DocumentUploadResponse response = documentServiceClient.uploadDocument(title, description, file);

        auditLogService.logCompleted(workflowId, WorkflowStep.UPLOAD, AgentType.DOCUMENT_ANALYZER,
                "upload_document", response);
        return response;
    }

    public ExtractionData extract(Long workflowId, Long documentId, String extractedText) {
        auditLogService.logStarted(workflowId, WorkflowStep.EXTRACT, AgentType.DOCUMENT_ANALYZER,
                "extract_structured_data", Map.of("documentId", documentId, "textLength", extractedText.length()));

        ExtractionData extraction = aiServiceClient.extractStructuredData(documentId, extractedText);

        auditLogService.logCompleted(workflowId, WorkflowStep.EXTRACT, AgentType.DOCUMENT_ANALYZER,
                "extract_structured_data", extraction);
        return extraction;
    }

    public String fetchExtractedText(Long documentId) {
        return documentServiceClient.getDocumentContent(documentId);
    }
}
