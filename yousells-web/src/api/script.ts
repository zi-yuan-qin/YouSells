import request from "@/utils/request";
import type { ApiResponse, PageResponse, IdResponse } from "@/types/api";
import type {
  ScriptCategory,
  ScriptItem,
  ScriptCreateRequest,
  ScriptUpdateRequest
} from "@/types/script";

export async function fetchScriptCategories(): Promise<ScriptCategory[]> {
  const response = await request.get<ApiResponse<ScriptCategory[]>>("/scripts/categories");
  return response.data.data;
}

export async function fetchScripts(params: {
  page?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
} = {}): Promise<PageResponse<ScriptItem>> {
  const response = await request.get<ApiResponse<PageResponse<ScriptItem>>>("/scripts", { params });
  return response.data.data;
}

export async function fetchScriptDetail(id: number): Promise<ScriptItem> {
  const response = await request.get<ApiResponse<ScriptItem>>(`/scripts/${id}`);
  return response.data.data;
}

export async function createScript(data: ScriptCreateRequest): Promise<IdResponse> {
  const response = await request.post<ApiResponse<IdResponse>>("/scripts", data);
  return response.data.data;
}

export async function updateScript(id: number, data: ScriptUpdateRequest): Promise<void> {
  await request.put<ApiResponse<null>>(`/scripts/${id}`, data);
}
