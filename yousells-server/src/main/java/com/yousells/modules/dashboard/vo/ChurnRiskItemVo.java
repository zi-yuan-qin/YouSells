package com.yousells.modules.dashboard.vo;

import java.time.LocalDateTime;
import java.util.List;

public record ChurnRiskItemVo(
        Long customerId,
        String customerName,
        String riskLevel,
        int riskScore,
        List<String> riskFactors,
        String suggestion,
        int silentDays,
        LocalDateTime lastFollowUpAt,
        LocalDateTime evaluatedAt
) {}
