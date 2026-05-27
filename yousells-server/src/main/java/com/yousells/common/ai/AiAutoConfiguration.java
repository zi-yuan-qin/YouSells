package com.yousells.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiConfigProperties.class)
public class AiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AiService aiService(AiConfigProperties properties, ObjectMapper objectMapper) {
        return new AiServiceImpl(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(AiConfigProperties properties, AiService aiService, ObjectMapper objectMapper) {
        return new EmbeddingServiceImpl(properties, aiService, objectMapper);
    }
}
