package com.yousells.modules.customer.service;

import com.yousells.modules.customer.vo.ChurnEvaluateResponse;
import com.yousells.modules.dashboard.vo.ChurnRiskResponse;

public interface ChurnRiskService {
    ChurnEvaluateResponse evaluateAll();
    ChurnRiskResponse getChurnRisks();
    void evaluateCustomer(Long customerId);
}
