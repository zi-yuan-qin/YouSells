package com.yousells.common.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RestClient llmClient;
    private final RestClient embeddingClient;
    private final AiConfigProperties properties;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(AiConfigProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.llmClient = buildRestClient(properties.llm().endpoint(), properties.llm().apiKey(), properties.llm().timeoutSeconds());
        this.embeddingClient = buildRestClient(properties.embedding().endpoint(), properties.embedding().apiKey(), 30);
    }

    // ── public API ────────────────────────────────────────────

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (properties.mock().enabled()) {
            return mockGenerate(userPrompt);
        }
        return callWithRetry(() -> doGenerate(systemPrompt, userPrompt, false));
    }

    @Override
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> schema) {
        if (properties.mock().enabled()) {
            return mockStructured(userPrompt, schema);
        }
        String raw = callWithRetry(() -> doGenerate(systemPrompt, userPrompt, true));
        String json = extractJson(raw);
        try {
            return objectMapper.readValue(json, schema);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse structured AI response, raw: {}", raw, e);
            throw new AiException("AI returned invalid JSON for " + schema.getSimpleName());
        }
    }

    @Override
    public float[] embed(String text) {
        if (properties.mock().enabled()) {
            return mockEmbed();
        }
        return callWithRetry(() -> doEmbed(text));
    }

    // ── LLM call ──────────────────────────────────────────────

    private String doGenerate(String systemPrompt, String userPrompt, boolean jsonMode) {
        var requestBody = Map.of(
                "model", properties.llm().model(),
                "temperature", properties.llm().temperature(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        var body = jsonMode
                ? withJsonSchema(requestBody)
                : requestBody;

        String response = llmClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (JsonProcessingException e) {
            throw new AiException("Failed to parse LLM response");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> withJsonSchema(Map<String, Object> body) {
        var mutable = new java.util.LinkedHashMap<>(body);
        mutable.put("response_format", Map.of("type", "json_object"));
        return mutable;
    }

    // ── Embedding call ────────────────────────────────────────

    private float[] doEmbed(String text) {
        var requestBody = Map.of(
                "model", properties.embedding().model(),
                "input", text
        );

        String response = embeddingClient.post()
                .uri("/v1/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedding = root.path("data").get(0).path("embedding");
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = (float) embedding.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            throw new AiException("Failed to parse embedding response");
        }
    }

    // ── retry ─────────────────────────────────────────────────

    private <T> T callWithRetry(java.util.function.Supplier<T> callable) {
        int maxRetries = properties.llm().maxRetries();
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return callable.get();
            } catch (AiException e) {
                throw e;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    long waitMs = (long) Math.pow(2, attempt) * 500;
                    log.warn("AI call failed (attempt {}), retrying in {}ms", attempt + 1, waitMs, e);
                    try { Thread.sleep(waitMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new AiException("AI call failed after " + (maxRetries + 1) + " attempts", lastException);
    }

    // ── JSON helpers ──────────────────────────────────────────

    static String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    // ── mock (dev) ────────────────────────────────────────────

    private String mockGenerate(String userPrompt) {
        log.debug("AiService mock generate, prompt preview: {}", userPrompt.substring(0, Math.min(userPrompt.length(), 80)));
        return "[AI Mock] 这是模拟的 AI 文本回复。配置 app.ai.mock.enabled=false 可启用真实 LLM 调用。";
    }

    private <T> T mockStructured(String userPrompt, Class<T> schema) {
        log.debug("AiService mock structured for {}, prompt preview: {}", schema.getSimpleName(),
                userPrompt.substring(0, Math.min(userPrompt.length(), 80)));
        try {
            return objectMapper.readValue("{}", schema);
        } catch (JsonProcessingException e) {
            throw new AiException("Mock structured generation failed for " + schema.getSimpleName());
        }
    }

    private float[] mockEmbed() {
        return new float[1536];
    }

    // ── factory ───────────────────────────────────────────────

    private static RestClient buildRestClient(String endpoint, String apiKey, int timeoutSeconds) {
        return RestClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory())
                .build();
    }
}
