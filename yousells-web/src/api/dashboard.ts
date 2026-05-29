import request from "@/utils/request";
import type { ApiResponse } from "@/types/api";
import type { DashboardOverview } from "@/types/dashboard";
import type { ChurnRiskResponse, ChurnEvaluateResponse } from "@/types/churn";

export async function fetchDashboardOverview(): Promise<DashboardOverview> {
  const response = await request.get<ApiResponse<DashboardOverview>>("/dashboard/overview");
  return response.data.data;
}

export async function fetchChurnRisks(): Promise<ChurnRiskResponse> {
  const response = await request.get<ApiResponse<ChurnRiskResponse>>("/dashboard/churn-risks");
  return response.data.data;
}

export async function triggerChurnEvaluate(): Promise<ChurnEvaluateResponse> {
  const response = await request.post<ApiResponse<ChurnEvaluateResponse>>("/customers/churn/evaluate");
  return response.data.data;
}
