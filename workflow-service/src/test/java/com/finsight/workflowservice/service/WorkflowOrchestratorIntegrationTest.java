package com.finsight.workflowservice.service;

import com.finsight.workflowservice.client.AIServiceClient;
import com.finsight.workflowservice.client.DocumentServiceClient;
import com.finsight.workflowservice.dto.DocumentUploadResponse;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.dto.WorkflowResponse;
import com.finsight.workflowservice.kafka.producer.WorkflowEventProducer;
import com.finsight.workflowservice.model.WorkflowStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class WorkflowOrchestratorIntegrationTest {

    @MockBean DocumentServiceClient documentServiceClient;
    @MockBean AIServiceClient aiServiceClient;
    @MockBean WorkflowEventProducer eventProducer;

    @Autowired WorkflowOrchestrator orchestrator;

    @Test
    void executes_end_to_end_workflow() {
        DocumentUploadResponse upload = new DocumentUploadResponse();
        upload.setDocumentId(7L);
        upload.setUploadedAt(Instant.now());
        upload.setExtractedChars(1234);
        upload.setStatus("EXTRACTED");
        when(documentServiceClient.uploadDocument(anyString(), anyString(), any())).thenReturn(upload);
        when(documentServiceClient.getDocumentContent(7L)).thenReturn("APPLICANT: Jane Doe ...");

        ExtractionData extraction = ExtractionData.builder()
                .documentId(7L)
                .applicantName("Jane Doe")
                .address("123 Main St")
                .income("80000")
                .loanAmount("100000")
                .build();
        when(aiServiceClient.extractStructuredData(anyLong(), anyString())).thenReturn(extraction);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf".getBytes());
        WorkflowResponse response = orchestrator.execute("Loan App", "Test", file, 0L);

        assertThat(response.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(response.getDecision().getRecommendation()).isEqualTo("APPROVE");
        assertThat(response.getCompliance().isCompliant()).isTrue();
        assertThat(response.getAuditLog()).isNotEmpty();
    }
}
