package com.yousells.modules.report.dto;

public record WeeklyReportCreateRequest(
        String weekKey,
        String summary,
        String issues,
        String nextWeekPlan
) {
}
