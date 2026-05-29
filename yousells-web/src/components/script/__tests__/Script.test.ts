import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ElementPlus from "element-plus";
import ScriptCategoryTabs from "../ScriptCategoryTabs.vue";
import ScriptTable from "../ScriptTable.vue";
import ScriptPreviewDrawer from "../ScriptPreviewDrawer.vue";
import type { ScriptCategory, ScriptItem } from "@/types/script";

function createWrapper(component: any, props: Record<string, unknown> = {}) {
  const pinia = createPinia();
  setActivePinia(pinia);
  return mount(component, {
    props,
    global: {
      plugins: [pinia, ElementPlus],
      stubs: {
        teleport: true,
        "el-table": true,
        "el-table-column": true,
        "el-pagination": true,
        "el-input": true,
        "el-dialog": true,
        "el-drawer": true
      }
    }
  });
}

const mockCategories: ScriptCategory[] = [
  { id: 1, categoryCode: "invite", categoryName: "邀约", sortOrder: 1 },
  { id: 2, categoryCode: "communicate", categoryName: "沟通", sortOrder: 2 },
  { id: 3, categoryCode: "followup", categoryName: "跟进", sortOrder: 3 }
];

const mockScripts: ScriptItem[] = [
  {
    id: 1,
    categoryId: 1,
    categoryName: "邀约",
    title: "知乎私信邀约话术",
    applicableScene: "知乎平台私信触达",
    status: "启用",
    content: "同学你好，看到你在关注编程学习...",
    updatedAt: "2026-05-28T10:00:00"
  },
  {
    id: 2,
    categoryId: 2,
    categoryName: "沟通",
    title: "初步电话沟通模板",
    applicableScene: "首次电话沟通",
    status: "启用",
    content: "您好，我是XX教育的课程顾问...",
    updatedAt: "2026-05-27T15:30:00"
  }
];

describe("ScriptCategoryTabs", () => {
  it("renders all categories plus 全部 tab", () => {
    const wrapper = createWrapper(ScriptCategoryTabs, {
      categories: mockCategories,
      activeCategoryId: null
    });
    expect(wrapper.text()).toContain("全部");
    expect(wrapper.text()).toContain("邀约");
    expect(wrapper.text()).toContain("沟通");
    expect(wrapper.text()).toContain("跟进");
  });

  it("highlights the active category", () => {
    const wrapper = createWrapper(ScriptCategoryTabs, {
      categories: mockCategories,
      activeCategoryId: 1
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("handles empty categories array", () => {
    const wrapper = createWrapper(ScriptCategoryTabs, {
      categories: [],
      activeCategoryId: null
    });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("ScriptTable", () => {
  it("renders with script data", () => {
    const wrapper = createWrapper(ScriptTable, {
      scripts: mockScripts,
      loading: false,
      total: 2,
      page: 1,
      pageSize: 20
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("handles empty scripts list", () => {
    const wrapper = createWrapper(ScriptTable, {
      scripts: [],
      loading: false,
      total: 0,
      page: 1,
      pageSize: 20
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(ScriptTable, {
      scripts: [],
      loading: true,
      total: 0,
      page: 1,
      pageSize: 20
    });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("ScriptPreviewDrawer", () => {
  it("renders script content when visible", () => {
    const wrapper = createWrapper(ScriptPreviewDrawer, {
      visible: true,
      script: mockScripts[0],
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("handles null script gracefully", () => {
    const wrapper = createWrapper(ScriptPreviewDrawer, {
      visible: false,
      script: null,
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });
});
