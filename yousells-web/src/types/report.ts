export interface DailyReport {
  id: number;
  reportDate: string;
  userDisplayName: string;
  todayWork: string;
  issues: string | null;
  tomorrowPlan: string;
}

export interface WeeklyReport {
  id: number;
  weekKey: string;
  userDisplayName: string;
  weeklySummary: string;
  issues: string | null;
  nextWeekPlan: string;
}

export interface DailyReportCreateRequest {
  reportDate: string;
  todayWork: string;
  issues?: string | null;
  tomorrowPlan: string;
}

export interface WeeklyReportCreateRequest {
  weekKey: string;
  weeklySummary: string;
  issues?: string | null;
  nextWeekPlan: string;
}
