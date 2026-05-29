import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ElementPlus from "element-plus";
import DailyReportForm from "../DailyReportForm.vue";
import WeeklyReportForm from "../WeeklyReportForm.vue";
import ReportHistoryPanel from "../ReportHistoryPanel.vue";
import type { DailyReport, WeeklyReport } from "@/types/report";

function createWrapper(component: any, props: Record<string, unknown> = {}) {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(component, {
    props,
    global: {
      plugins: [pinia, ElementPlus],
      stubs: {
        teleport: true,
        "el-date-picker": true,
        "el-select": true,
        "el-option": true,
        "el-pagination": true,
        "el-table": true,
        "el-table-column": true
      }
    }
  });
}

const mockDailyReport: DailyReport = {
  id: 1,
  reportDate: "2026-05-28",
  userRealName: "许润",
  newCustomerCount: 2,
  followUpCount: 5,
  progressAdvanceCount: 1,
  taskCompletedCount: 3,
  summary: "今天跟进5个客户，新增2个潜在客户，完成了3个任务",
  issues: "王同学家长还在犹豫价格",
  tomorrowPlan: "跟进王同学家长，准备周末的活动方案"
};

const mockWeeklyReport: WeeklyReport = {
  id: 1,
  weekKey: "2026-W22",
  userRealName: "许润",
  weekStart: "2026-05-25",
  weekEnd: "2026-05-31",
  newCustomerCount: 5,
  followUpCount: 12,
  progressAdvanceCount: 3,
  convertedCount: 1,
  taskCompletedCount: 8,
  summary: "本周重点推进了AI就业班的招生工作",
  issues: "知乎渠道获客成本偏高",
  nextWeekPlan: "加强校园地推，优化知乎投放"
};

const mockHistory: DailyReport[] = [
  { ...mockDailyReport, id: 1, reportDate: "2026-05-28" },
  { ...mockDailyReport, id: 2, reportDate: "2026-05-27", summary: "昨日工作总结" }
];

describe("DailyReportForm", () => {
  it("renders form fields", () => {
    const wrapper = createWrapper(DailyReportForm, {});
    expect(wrapper.text()).toContain("今日小结");
    expect(wrapper.text()).toContain("明日计划");
    expect(wrapper.text()).toContain("提交日报");
  });

  it("renders in edit mode", () => {
    const wrapper = createWrapper(DailyReportForm, {
      editReport: mockDailyReport
    });
    expect(wrapper.text()).toContain("编辑日报");
    expect(wrapper.text()).toContain("保存修改");
  });
});

describe("WeeklyReportForm", () => {
  it("renders form fields", () => {
    const wrapper = createWrapper(WeeklyReportForm, {});
    expect(wrapper.text()).toContain("本周总结");
    expect(wrapper.text()).toContain("下周计划");
    expect(wrapper.text()).toContain("提交周报");
  });

  it("renders in edit mode", () => {
    const wrapper = createWrapper(WeeklyReportForm, {
      editReport: mockWeeklyReport
    });
    expect(wrapper.text()).toContain("编辑周报");
    expect(wrapper.text()).toContain("保存修改");
  });

  it("accepts weekKey prop", () => {
    const wrapper = createWrapper(WeeklyReportForm, { weekKey: "2026-W20" });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("ReportHistoryPanel", () => {
  it("renders report list items", () => {
    const wrapper = createWrapper(ReportHistoryPanel, {
      reports: mockHistory,
      total: 2,
      page: 1,
      pageSize: 20,
      reportType: "daily",
      loading: false
    });
    expect(wrapper.text()).toContain("昨日工作总结");
  });

  it("handles empty reports", () => {
    const wrapper = createWrapper(ReportHistoryPanel, {
      reports: [],
      total: 0,
      page: 1,
      pageSize: 20,
      reportType: "daily",
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("handles loading state", () => {
    const wrapper = createWrapper(ReportHistoryPanel, {
      reports: mockHistory,
      total: 2,
      page: 1,
      pageSize: 20,
      reportType: "daily",
      loading: true
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("renders weekly type", () => {
    const weeklyHistory = [mockWeeklyReport];
    const wrapper = createWrapper(ReportHistoryPanel, {
      reports: weeklyHistory,
      total: 1,
      page: 1,
      pageSize: 20,
      reportType: "weekly",
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });
});
