package com.finsight.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Normalised DTO for any workflow event consumed from Kafka.
 * Fields are mapped from the raw Map payload produced by workflow-service.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowEventDto {

    /** The originating Kafka topic. */
    private String topic;

    /** Workflow instance ID. */
    private Long workflowId;

    /** Associated document ID. */
    private Long documentId;

    /** Workflow status at the time of the event, e.g. COMPLETED, FAILED. */
    private String status;

    /** Current step, e.g. UPLOAD, EXTRACT, VALIDATE, RECOMMEND. */
    private String step;

    /** Optional: applicant name extracted by AI service. */
    private String applicantName;

    /** Optional: error message if the workflow failed. */
    private String errorMessage;

    /** Server-side timestamp when the event was created. */
    private Instant eventTimestamp;
}
