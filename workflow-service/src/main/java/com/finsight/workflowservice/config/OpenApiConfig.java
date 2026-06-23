package com.finsight.workflowservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workflowServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinSight AI - Workflow Service")
                        .description("Orchestrates Document Analyzer, Compliance, and Decision agents through the Upload→Extract→Validate→Recommend flow.")
                        .version("v1")
                        .contact(new Contact().name("FinSight AI").email("dev@finsight.ai"))
                        .license(new License().name("MIT")));
    }
}
