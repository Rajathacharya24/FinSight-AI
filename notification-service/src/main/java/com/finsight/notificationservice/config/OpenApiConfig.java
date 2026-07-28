package com.finsight.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinSight AI — Notification Service API")
                        .description("Kafka-driven event notifications: email and webhook dispatch on workflow state changes.")
                        .version("1.0.0"));
    }
}
