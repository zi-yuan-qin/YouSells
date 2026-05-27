export interface AiInsight {
  customerId: number;
  intentTrend: "上升" | "平稳" | "下降";
  intentTrendReason: string;
  keyConcerns: string[];
  communicationStyle: string;
  conversionProbability: "高" | "中" | "低";
  conversionConfidence: number;
  nextActionSuggestion: string;
  summary: string;
  generatedAt: string;
}
