package com.yousells.common.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    private final RestClient qdrantClient;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final String collectionName;

    public EmbeddingServiceImpl(AiConfigProperties properties, AiService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
        this.collectionName = properties.qdrant().collectionScripts();
        this.qdrantClient = RestClient.builder()
                .baseUrl("http://" + properties.qdrant().host() + ":" + properties.qdrant().port())
                .defaultHeader("api-key", properties.qdrant().apiKey())
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory())
                .build();
    }

    @Override
    public void initScriptCollection() {
        try {
            qdrantClient.put()
                    .uri("/collections/{name}", collectionName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "vectors", Map.of(
                                    "size", 1536,
                                    "distance", "Cosine"
                            )
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Qdrant collection '{}' initialized", collectionName);
        } catch (Exception e) {
            log.debug("Qdrant collection '{}' may already exist: {}", collectionName, e.getMessage());
        }
    }

    @Override
    public void upsertScript(String id, String text, Map<String, Object> payload) {
        float[] vector = aiService.embed(text);
        try {
            var body = Map.of("points", List.of(Map.of(
                    "id", id,
                    "vector", toDoubleList(vector),
                    "payload", payload
            )));
            qdrantClient.put()
                    .uri("/collections/{name}/points", collectionName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to upsert script vector id={}", id, e);
        }
    }

    @Override
    public void deleteScript(String id) {
        try {
            qdrantClient.delete()
                    .uri("/collections/{name}/points/{id}", collectionName, id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete script vector id={}", id, e);
        }
    }

    @Override
    public List<SearchResult> searchSimilar(String queryText, int limit) {
        float[] vector = aiService.embed(queryText);
        var body = Map.of(
                "vector", toDoubleList(vector),
                "limit", limit,
                "with_payload", true
        );
        try {
            String response = qdrantClient.post()
                    .uri("/collections/{name}/points/search", collectionName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            List<SearchResult> results = new ArrayList<>();
            for (JsonNode point : root.path("result")) {
                String id = point.path("id").asText();
                double score = point.path("score").asDouble();
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = objectMapper.convertValue(point.path("payload"), Map.class);
                results.add(new SearchResult(id, score, payload != null ? payload : Collections.emptyMap()));
            }
            return results;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse Qdrant search response", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Qdrant search failed", e);
            return Collections.emptyList();
        }
    }

    private static List<Double> toDoubleList(float[] vector) {
        List<Double> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add((double) v);
        }
        return list;
    }
}
