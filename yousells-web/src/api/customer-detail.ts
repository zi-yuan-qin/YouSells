import request from "@/utils/request";
import type { ApiResponse } from "@/types/api";
import type {
  CustomerDetail,
  CustomerUpdateRequest,
  CustomerTagVo,
  CustomerNextFollowRequest
} from "@/types/customer-detail";

export async function fetchCustomerDetail(id: string | number): Promise<CustomerDetail> {
  const response = await request.get<ApiResponse<CustomerDetail>>(`/customers/${id}`);
  return response.data.data;
}

export async function updateCustomer(id: number, data: CustomerUpdateRequest): Promise<void> {
  await request.put<ApiResponse<null>>(`/customers/${id}`, data);
}

export async function fetchCustomerTags(): Promise<CustomerTagVo[]> {
  const response = await request.get<ApiResponse<CustomerTagVo[]>>("/customers/tags");
  return response.data.data;
}

export async function updateCustomerTags(id: number, tagIds: number[]): Promise<void> {
  await request.put<ApiResponse<null>>(`/customers/${id}/tags`, { tagIds });
}

export async function updateNextFollow(id: number, data: CustomerNextFollowRequest): Promise<void> {
  await request.put<ApiResponse<null>>(`/customers/${id}/next-follow`, data);
}
