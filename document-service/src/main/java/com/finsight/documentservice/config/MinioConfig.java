package com.finsight.documentservice.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO object store configuration.
 *
 * <p>MinioClient is only created when {@code minio.enabled=true} (default is false in dev/test).
 * When disabled, document-service falls back to the local filesystem storage path.
 */
@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${minio.bucket:finsight-documents}")
    private String bucket;

    @Bean
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
    public MinioClient minioClient() {
        log.info("[MinioConfig] Connecting to MinIO at {}, bucket={}", endpoint, bucket);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public MinioProperties minioProperties() {
        return new MinioProperties(endpoint, accessKey, secretKey, bucket,
                Boolean.parseBoolean(System.getProperty("minio.enabled", "false")));
    }

    public record MinioProperties(String endpoint, String accessKey, String secretKey,
                                  String bucket, boolean enabled) {}
}
