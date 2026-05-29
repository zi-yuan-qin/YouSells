import request from "@/utils/request";
import type { ApiResponse } from "@/types/api";
import type { AiInsight } from "@/types/ai-insight";
import type { CustomerDetail } from "@/types/customer-detail";

export async function fetchCustomerDetail(id: string | number): Promise<CustomerDetail> {
  const response = await request.get<ApiResponse<CustomerDetail>>(`/customers/${id}`);
  return response.data.data;
}

export async function updateCustomer(
  id: string | number,
  data: Partial<CustomerDetail> & { inviterUserId: number; ownerUserId: number }
): Promise<void> {
  await request.put<ApiResponse<null>>(`/customers/${id}`, data);
}

export async function fetchCustomerAiInsight(customerId: string | number): Promise<AiInsight> {
  const response = await request.get<ApiResponse<AiInsight>>(`/customers/${customerId}/ai-insight`);
  return response.data.data;
}

export async function refreshCustomerAiInsight(customerId: string | number): Promise<AiInsight> {
  const response = await request.post<ApiResponse<AiInsight>>(`/customers/${customerId}/ai-insight/refresh`);
  return response.data.data;
}
