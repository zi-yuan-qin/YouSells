package com.yousells.modules.customer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiInsightResponse(
        Long customerId,
        String intentTrend,
        String intentTrendReason,
        List<String> keyConcerns,
        String communicationStyle,
        String conversionProbability,
        Integer conversionConfidence,
        String nextActionSuggestion,
        String summary,
        LocalDateTime generatedAt
) {

    public static AiInsightResponse empty(Long customerId) {
        return new AiInsightResponse(
                customerId,
                "平稳",
                "暂无足够跟进记录",
                List.of(),
                "未知",
                "低",
                0,
                "建议先添加跟进记录",
                "暂无足够跟进记录，无法生成 AI 洞察",
                LocalDateTime.now().withNano(0)
        );
    }
}
