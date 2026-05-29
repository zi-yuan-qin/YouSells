export interface ChurnRiskItem {
  customerId: number;
  customerName: string;
  riskLevel: "high" | "medium";
  riskScore: number;
  riskFactors: string[];
  suggestion: string;
  silentDays: number;
  lastFollowUpAt: string | null;
  evaluatedAt: string;
}

export interface ChurnRiskResponse {
  highRisk: ChurnRiskItem[];
  mediumRisk: ChurnRiskItem[];
  total: number;
  evaluatedAt: string | null;
}

export interface ChurnEvaluateResponse {
  evaluatedCount: number;
  highRiskCount: number;
  mediumRiskCount: number;
  evaluatedAt: string;
}
