import request from "@/utils/request";
import type { ApiResponse, PageResponse, IdResponse } from "@/types/api";
import type { FollowUpRecord, FollowUpCreateRequest } from "@/types/followup";

export async function fetchFollowUps(customerId: string | number): Promise<PageResponse<FollowUpRecord>> {
  const response = await request.get<ApiResponse<PageResponse<FollowUpRecord>>>("/follow-ups", {
    params: {
      customerId,
      page: 1,
      pageSize: 20
    }
  });
  return response.data.data;
}

export async function createFollowUp(data: FollowUpCreateRequest): Promise<IdResponse> {
  const response = await request.post<ApiResponse<IdResponse>>("/follow-ups", data);
  return response.data.data;
}
