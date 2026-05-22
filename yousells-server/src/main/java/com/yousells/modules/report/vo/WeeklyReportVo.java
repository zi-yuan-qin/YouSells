package com.yousells.modules.report.vo;

import com.yousells.modules.report.convert.ReportConvert.WeeklyAggregation;

import java.time.LocalDate;

public record WeeklyReportVo(
        Long id,
        String weekKey,
        Long userId,
        String userRealName,
        LocalDate weekStart,
        LocalDate weekEnd,
        int newCustomerCount,
        int followUpCount,
        int progressAdvanceCount,
        int convertedCount,
        int taskCompletedCount,
        String progressDetails,
        String convertedDetails,
        String taskCompletedDetails,
        String summary,
        String issues,
        String nextWeekPlan
) {
    public static WeeklyReportVo preview(String weekKey, Long userId, String realName, WeeklyAggregation agg) {
        return new WeeklyReportVo(null, weekKey, userId, realName,
                agg.weekStart(), agg.weekEnd(),
                agg.newCustomerCount(), agg.followUpCount(), agg.progressAdvanceCount(),
                agg.convertedCount(), agg.taskCompletedCount(),
                agg.progressDetails(), agg.convertedDetails(), agg.taskCompletedDetails(),
                null, null, null);
    }
}
