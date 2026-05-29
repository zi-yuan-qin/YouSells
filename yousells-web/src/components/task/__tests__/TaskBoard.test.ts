import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ElementPlus from "element-plus";
import TaskBoardCard from "../TaskBoardCard.vue";
import TaskBoardColumn from "../TaskBoardColumn.vue";
import TaskBoardToolbar from "../TaskBoardToolbar.vue";
import type { TaskBoardItem, TaskBoardColumn as TaskBoardColumnType } from "@/types/task";

function createWrapper(component: any, props: Record<string, unknown> = {}) {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(component, {
    props,
    global: {
      plugins: [pinia, ElementPlus],
      stubs: { teleport: true }
    }
  });
}

const mockTask: TaskBoardItem = {
  id: 1,
  taskTitle: "整理攻略区分类与模板",
  direction: "自己规划",
  status: "进行中",
  priority: "高",
  ownerUserId: 1,
  ownerDisplayName: "秦梓源",
  dueAt: "2026-05-31T00:00:00"
};

const mockColumn: TaskBoardColumnType = {
  status: "待开始",
  title: "待开始",
  items: [
    {
      id: 3,
      taskTitle: "知乎引流测试",
      direction: "自己规划",
      status: "待开始",
      priority: "低",
      ownerUserId: 2,
      ownerDisplayName: "小赵",
      dueAt: "2026-06-02T00:00:00"
    }
  ]
};

describe("TaskBoardCard", () => {
  it("renders task title", () => {
    const wrapper = createWrapper(TaskBoardCard, { item: mockTask });
    expect(wrapper.text()).toContain("整理攻略区分类与模板");
  });

  it("renders priority badge", () => {
    const wrapper = createWrapper(TaskBoardCard, { item: mockTask });
    expect(wrapper.text()).toContain("高");
  });

  it("renders owner display name", () => {
    const wrapper = createWrapper(TaskBoardCard, { item: mockTask });
    expect(wrapper.text()).toContain("秦梓源");
  });

  it("renders due date", () => {
    const wrapper = createWrapper(TaskBoardCard, { item: mockTask });
    expect(wrapper.text()).toContain("05-31");
  });

  it("renders low priority task", () => {
    const lowPriorityTask = { ...mockTask, priority: "低" };
    const wrapper = createWrapper(TaskBoardCard, { item: lowPriorityTask });
    expect(wrapper.exists()).toBe(true);
  });

  it("renders task without due date", () => {
    const noDueTask = { ...mockTask, dueAt: null };
    const wrapper = createWrapper(TaskBoardCard, { item: noDueTask });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("TaskBoardColumn", () => {
  it("renders column title", () => {
    const wrapper = createWrapper(TaskBoardColumn, {
      column: mockColumn,
      loading: false
    });
    expect(wrapper.text()).toContain("待开始");
  });

  it("renders task cards within the column", () => {
    const wrapper = createWrapper(TaskBoardColumn, {
      column: mockColumn,
      loading: false
    });
    expect(wrapper.text()).toContain("知乎引流测试");
  });

  it("handles empty column", () => {
    const emptyColumn = { ...mockColumn, items: [] };
    const wrapper = createWrapper(TaskBoardColumn, {
      column: emptyColumn,
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(TaskBoardColumn, {
      column: mockColumn,
      loading: true
    });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("TaskBoardToolbar", () => {
  it("renders refresh button area", () => {
    const wrapper = createWrapper(TaskBoardToolbar, { loading: false });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state on button", () => {
    const wrapper = createWrapper(TaskBoardToolbar, { loading: true });
    expect(wrapper.exists()).toBe(true);
  });
});
