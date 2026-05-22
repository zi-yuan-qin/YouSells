package com.yousells.modules.report.vo;

import com.yousells.modules.report.convert.ReportConvert.DailyAggregation;

import java.time.LocalDate;

public record DailyReportVo(
        Long id,
        LocalDate reportDate,
        Long userId,
        String userRealName,
        int newCustomerCount,
        int followUpCount,
        int progressAdvanceCount,
        int taskCompletedCount,
        String progressDetails,
        String taskCompletedDetails,
        String summary,
        String issues,
        String tomorrowPlan
) {
    public static DailyReportVo preview(LocalDate date, Long userId, String realName, DailyAggregation agg) {
        return new DailyReportVo(null, date, userId, realName,
                agg.newCustomerCount(), agg.followUpCount(), agg.progressAdvanceCount(),
                agg.taskCompletedCount(), agg.progressDetails(), agg.taskCompletedDetails(),
                null, null, null);
    }
}
