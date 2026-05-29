package com.yousells.modules.dashboard.vo;

import java.time.LocalDateTime;
import java.util.List;

public record ChurnRiskResponse(
        List<ChurnRiskItemVo> highRisk,
        List<ChurnRiskItemVo> mediumRisk,
        int total,
        LocalDateTime evaluatedAt
) {}
