package com.finsight.workflowservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "finsight.workflow")
public class WorkflowProperties {

    private String documentServiceUrl = "http://document-service";
    private String aiServiceUrl = "http://ai-service";
}
