package com.finsight.ai.service.config;

import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class MockEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            private final List<Double> dummyEmbedding;
            private final float[] dummyFloatEmbedding;

            {
                List<Double> list = new ArrayList<>(1536);
                float[] arr = new float[1536];
                for (int i = 0; i < 1536; i++) {
                    list.add(0.0);
                    arr[i] = 0.0f;
                }
                dummyEmbedding = Collections.unmodifiableList(list);
                dummyFloatEmbedding = arr;
            }

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = new ArrayList<>();
                List<String> instructions = request.getInstructions();
                for (int i = 0; i < instructions.size(); i++) {
                    embeddings.add(new Embedding(dummyEmbedding, i));
                }
                return new EmbeddingResponse(embeddings);
            }

            @Override
            public float[] embed(String text) {
                return dummyFloatEmbedding;
            }
        };
    }
}
