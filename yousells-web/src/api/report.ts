import request from "@/utils/request";
import type { ApiResponse, PageResponse, IdResponse } from "@/types/api";
import type {
  DailyReport,
  WeeklyReport,
  DailyReportCreateRequest,
  WeeklyReportCreateRequest
} from "@/types/report";

export async function fetchDailyReports(page = 1, pageSize = 20): Promise<PageResponse<DailyReport>> {
  const response = await request.get<ApiResponse<PageResponse<DailyReport>>>("/reports/daily", {
    params: { page, pageSize }
  });
  return response.data.data;
}

export async function createDailyReport(data: DailyReportCreateRequest): Promise<IdResponse> {
  const response = await request.post<ApiResponse<IdResponse>>("/reports/daily", data);
  return response.data.data;
}

export async function fetchWeeklyReports(page = 1, pageSize = 20): Promise<PageResponse<WeeklyReport>> {
  const response = await request.get<ApiResponse<PageResponse<WeeklyReport>>>("/reports/weekly", {
    params: { page, pageSize }
  });
  return response.data.data;
}

export async function createWeeklyReport(data: WeeklyReportCreateRequest): Promise<IdResponse> {
  const response = await request.post<ApiResponse<IdResponse>>("/reports/weekly", data);
  return response.data.data;
}
