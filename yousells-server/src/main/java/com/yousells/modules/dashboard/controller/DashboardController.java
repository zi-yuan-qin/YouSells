package com.yousells.modules.dashboard.controller;

import com.yousells.common.response.ApiResponse;
import com.yousells.modules.customer.service.ChurnRiskService;
import com.yousells.modules.dashboard.service.DashboardService;
import com.yousells.modules.dashboard.vo.ChurnRiskResponse;
import com.yousells.modules.dashboard.vo.DashboardOverviewVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ChurnRiskService churnRiskService;

    public DashboardController(DashboardService dashboardService, ChurnRiskService churnRiskService) {
        this.dashboardService = dashboardService;
        this.churnRiskService = churnRiskService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewVo> overview() {
        return ApiResponse.success(dashboardService.getOverview());
    }

    @GetMapping("/churn-risks")
    public ApiResponse<ChurnRiskResponse> churnRisks() {
        return ApiResponse.success(churnRiskService.getChurnRisks());
    }
}
