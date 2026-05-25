import request from "@/utils/request";
import type { ApiResponse, PageResponse, IdResponse } from "@/types/api";
import type { TaskBoardColumn, TaskBoardItem, TaskCreateRequest, TaskUpdateRequest, TaskStatusUpdateRequest } from "@/types/task";

export async function fetchTasks(params: { page?: number; pageSize?: number; status?: string } = {}): Promise<PageResponse<TaskBoardItem>> {
  const response = await request.get<ApiResponse<PageResponse<TaskBoardItem>>>("/tasks", { params });
  return response.data.data;
}

export async function fetchTaskBoard(): Promise<TaskBoardColumn[]> {
  const response = await request.get<ApiResponse<TaskBoardColumn[]>>("/tasks/board");
  return response.data.data;
}

export async function createTask(data: TaskCreateRequest): Promise<IdResponse> {
  const response = await request.post<ApiResponse<IdResponse>>("/tasks", data);
  return response.data.data;
}

export async function updateTask(id: number, data: TaskUpdateRequest): Promise<void> {
  await request.put<ApiResponse<null>>(`/tasks/${id}`, data);
}

export async function updateTaskStatus(id: number, data: TaskStatusUpdateRequest): Promise<void> {
  await request.put<ApiResponse<null>>(`/tasks/${id}/status`, data);
}
