package com.yousells.modules.customer.dto;

import lombok.Data;

import java.util.List;

@Data
public class RiskAnalysis {
    private List<String> riskFactors;
    private String suggestion;
}
