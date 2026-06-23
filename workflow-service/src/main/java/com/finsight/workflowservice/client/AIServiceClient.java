package com.finsight.workflowservice.client;

import com.finsight.workflowservice.config.WorkflowProperties;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.dto.ExtractionRequest;
import com.finsight.workflowservice.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class AIServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final WorkflowProperties properties;

    public ExtractionData extractStructuredData(Long documentId, String extractedText) {
        try {
            ExtractionRequest request = new ExtractionRequest();
            request.setExtractedText(extractedText);

            ExtractionData response = restClientBuilder.build()
                    .post()
                    .uri(properties.getAiServiceUrl() + "/api/v1/extract?documentId={documentId}", documentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ExtractionData.class);

            if (response == null) {
                throw new WorkflowException("AI extraction returned empty response");
            }
            return response;
        } catch (RestClientException e) {
            throw new WorkflowException("Failed to extract structured data", e);
        }
    }
}
