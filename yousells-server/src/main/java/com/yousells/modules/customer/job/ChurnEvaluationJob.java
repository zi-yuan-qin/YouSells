package com.yousells.modules.customer.job;

import com.yousells.modules.customer.service.ChurnRiskService;
import com.yousells.modules.customer.vo.ChurnEvaluateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChurnEvaluationJob {

    private final ChurnRiskService churnRiskService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void evaluate() {
        log.info("开始每日流失风险评估...");
        try {
            ChurnEvaluateResponse result = churnRiskService.evaluateAll();
            log.info("流失评估完成：{} 人评估，{} 高风险，{} 中风险",
                    result.evaluatedCount(), result.highRiskCount(), result.mediumRiskCount());
        } catch (Exception e) {
            log.error("流失评估失败", e);
        }
    }
}
