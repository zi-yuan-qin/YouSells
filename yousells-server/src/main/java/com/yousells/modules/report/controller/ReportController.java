package com.yousells.modules.report.controller;

import com.yousells.common.response.ApiResponse;
import com.yousells.common.response.IdResponse;
import com.yousells.common.response.PageResponse;
import com.yousells.modules.report.dto.DailyReportCreateRequest;
import com.yousells.modules.report.dto.WeeklyReportCreateRequest;
import com.yousells.modules.report.service.ReportService;
import com.yousells.modules.report.vo.DailyReportVo;
import com.yousells.modules.report.vo.WeeklyReportVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ApiResponse<DailyReportVo> daily(@RequestParam LocalDate date) {
        return ApiResponse.success(reportService.getDailyReport(date));
    }

    @GetMapping("/daily/history")
    public ApiResponse<PageResponse<DailyReportVo>> dailyHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(reportService.pageDailyReports(page, pageSize));
    }

    @PostMapping("/daily")
    public ApiResponse<IdResponse> createDaily(@Valid @RequestBody DailyReportCreateRequest request) {
        return ApiResponse.success(new IdResponse(reportService.createDailyReport(request)));
    }

    @PutMapping("/daily/{id}")
    public ApiResponse<Void> updateDaily(@PathVariable Long id,
                                          @Valid @RequestBody DailyReportCreateRequest request) {
        reportService.updateDailyReport(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/weekly")
    public ApiResponse<WeeklyReportVo> weekly(@RequestParam String week) {
        return ApiResponse.success(reportService.getWeeklyReport(week));
    }

    @GetMapping("/weekly/history")
    public ApiResponse<PageResponse<WeeklyReportVo>> weeklyHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(reportService.pageWeeklyReports(page, pageSize));
    }

    @PostMapping("/weekly")
    public ApiResponse<IdResponse> createWeekly(@Valid @RequestBody WeeklyReportCreateRequest request) {
        return ApiResponse.success(new IdResponse(reportService.createWeeklyReport(request)));
    }

    @PutMapping("/weekly/{id}")
    public ApiResponse<Void> updateWeekly(@PathVariable Long id,
                                           @Valid @RequestBody WeeklyReportCreateRequest request) {
        reportService.updateWeeklyReport(id, request);
        return ApiResponse.success();
    }
}
