package com.yousells.modules.report.service;

import com.yousells.common.response.PageResponse;
import com.yousells.modules.report.dto.DailyReportCreateRequest;
import com.yousells.modules.report.dto.WeeklyReportCreateRequest;
import com.yousells.modules.report.vo.DailyReportVo;
import com.yousells.modules.report.vo.WeeklyReportVo;

import java.time.LocalDate;

public interface ReportService {

    DailyReportVo getDailyReport(LocalDate date);

    PageResponse<DailyReportVo> pageDailyReports(int page, int pageSize);

    Long createDailyReport(DailyReportCreateRequest request);

    void updateDailyReport(Long id, DailyReportCreateRequest request);

    WeeklyReportVo getWeeklyReport(String weekKey);

    PageResponse<WeeklyReportVo> pageWeeklyReports(int page, int pageSize);

    Long createWeeklyReport(WeeklyReportCreateRequest request);

    void updateWeeklyReport(Long id, WeeklyReportCreateRequest request);
}
