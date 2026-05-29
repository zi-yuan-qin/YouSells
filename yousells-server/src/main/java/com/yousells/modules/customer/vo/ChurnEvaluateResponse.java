package com.yousells.modules.customer.vo;

import java.time.LocalDateTime;

public record ChurnEvaluateResponse(
        int evaluatedCount,
        int highRiskCount,
        int mediumRiskCount,
        LocalDateTime evaluatedAt
) {}
