package com.yousells.common.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiConfigProperties(
        LlmConfig llm,
        EmbeddingConfig embedding,
        QdrantConfig qdrant,
        MockConfig mock
) {
    public record LlmConfig(
            String endpoint,
            String apiKey,
            String model,
            double temperature,
            int maxRetries,
            int timeoutSeconds
    ) {}

    public record EmbeddingConfig(
            String endpoint,
            String apiKey,
            String model
    ) {}

    public record QdrantConfig(
            String host,
            int port,
            String apiKey,
            String collectionScripts
    ) {}

    public record MockConfig(
            boolean enabled
    ) {}
}
