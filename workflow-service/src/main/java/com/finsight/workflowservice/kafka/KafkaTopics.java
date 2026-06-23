package com.finsight.workflowservice.kafka;

public final class KafkaTopics {

    public static final String DOCUMENT_UPLOADED = "finsight.document.uploaded";
    public static final String EXTRACTION_COMPLETED = "finsight.extraction.completed";
    public static final String ANALYSIS_COMPLETED = "finsight.analysis.completed";

    public static final String DOCUMENT_UPLOADED_DLT = DOCUMENT_UPLOADED + ".DLT";
    public static final String EXTRACTION_COMPLETED_DLT = EXTRACTION_COMPLETED + ".DLT";
    public static final String ANALYSIS_COMPLETED_DLT = ANALYSIS_COMPLETED + ".DLT";

    private KafkaTopics() {}
}
