package com.yousells.modules.report.convert;

import com.yousells.modules.report.dto.DailyReportCreateRequest;
import com.yousells.modules.report.dto.WeeklyReportCreateRequest;
import com.yousells.modules.report.entity.DailyReportEntity;
import com.yousells.modules.report.entity.WeeklyReportEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

public final class ReportConvert {

    private ReportConvert() {
    }

    public record DailyAggregation(
            int newCustomerCount, int followUpCount, int progressAdvanceCount,
            int taskCompletedCount, String progressDetails, String taskCompletedDetails) {
    }

    public record WeeklyAggregation(
            int newCustomerCount, int followUpCount, int progressAdvanceCount,
            int convertedCount, int taskCompletedCount,
            String progressDetails, String convertedDetails, String taskCompletedDetails,
            LocalDate weekStart, LocalDate weekEnd) {
    }

    public record WeekRange(LocalDate weekStart, LocalDate weekEnd) {
    }

    public static WeekRange resolveWeekRange(String weekKey) {
        int year = Integer.parseInt(weekKey.substring(0, 4));
        int week = Integer.parseInt(weekKey.substring(6));
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        LocalDate weekStart = jan1.with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return new WeekRange(weekStart, weekEnd);
    }

    public static DailyReportEntity toDailyEntity(DailyReportCreateRequest request, Long userId,
                                                   DailyAggregation agg) {
        DailyReportEntity entity = new DailyReportEntity();
        entity.setReportDate(request.reportDate());
        entity.setUserId(userId);
        entity.setSummary(request.summary());
        entity.setIssues(request.issues());
        entity.setTomorrowPlan(request.tomorrowPlan());
        entity.setNewCustomerCount(agg.newCustomerCount());
        entity.setFollowUpCount(agg.followUpCount());
        entity.setProgressAdvanceCount(agg.progressAdvanceCount());
        entity.setTaskCompletedCount(agg.taskCompletedCount());
        entity.setProgressDetails(agg.progressDetails());
        entity.setTaskCompletedDetails(agg.taskCompletedDetails());
        return entity;
    }

    public static void applyDailyUpdate(DailyReportEntity entity, DailyReportCreateRequest request) {
        entity.setReportDate(request.reportDate());
        entity.setSummary(request.summary());
        entity.setIssues(request.issues());
        entity.setTomorrowPlan(request.tomorrowPlan());
    }

    public static WeeklyReportEntity toWeeklyEntity(WeeklyReportCreateRequest request, Long userId,
                                                     WeeklyAggregation agg) {
        WeeklyReportEntity entity = new WeeklyReportEntity();
        entity.setWeekKey(request.weekKey());
        entity.setUserId(userId);
        entity.setSummary(request.summary());
        entity.setIssues(request.issues());
        entity.setNextWeekPlan(request.nextWeekPlan());
        entity.setWeekStart(agg.weekStart());
        entity.setWeekEnd(agg.weekEnd());
        entity.setNewCustomerCount(agg.newCustomerCount());
        entity.setFollowUpCount(agg.followUpCount());
        entity.setProgressAdvanceCount(agg.progressAdvanceCount());
        entity.setConvertedCount(agg.convertedCount());
        entity.setTaskCompletedCount(agg.taskCompletedCount());
        entity.setProgressDetails(agg.progressDetails());
        entity.setConvertedDetails(agg.convertedDetails());
        entity.setTaskCompletedDetails(agg.taskCompletedDetails());
        return entity;
    }

    public static void applyWeeklyUpdate(WeeklyReportEntity entity, WeeklyReportCreateRequest request) {
        entity.setWeekKey(request.weekKey());
        entity.setSummary(request.summary());
        entity.setIssues(request.issues());
        entity.setNextWeekPlan(request.nextWeekPlan());
    }
}
