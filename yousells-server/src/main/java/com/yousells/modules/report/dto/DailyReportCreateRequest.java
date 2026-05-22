package com.yousells.modules.report.dto;

import java.time.LocalDate;

public record DailyReportCreateRequest(
        LocalDate reportDate,
        String summary,
        String issues,
        String tomorrowPlan
) {
}
