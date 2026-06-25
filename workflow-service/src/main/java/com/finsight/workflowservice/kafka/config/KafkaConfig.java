package com.finsight.workflowservice.kafka.config;

import com.finsight.workflowservice.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_UPLOADED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic extractionCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.EXTRACTION_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic analysisCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.ANALYSIS_COMPLETED).partitions(3).replicas(1).build();
    }
}
