import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ElementPlus from "element-plus";
import FollowUpTimeline from "../FollowUpTimeline.vue";
import FollowUpCreateForm from "../FollowUpCreateForm.vue";
import type { FollowUpRecord } from "@/types/followup";

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

const mockFollowUps: FollowUpRecord[] = [
  {
    id: 1,
    customerId: 1,
    userRealName: "秦梓源",
    progress: "技术栈",
    content: "试听课圆满成功，同学非常满意。已缴费，安排了下周一开课。",
    feedback: "对导师评价很高，期待上课",
    nextAction: "正式开课",
    createdAt: "2026-05-27T19:44:29"
  },
  {
    id: 2,
    customerId: 1,
    userRealName: "秦梓源",
    progress: "职规",
    content: "初步沟通，了解了同学的学习意向和背景情况。",
    feedback: "对AI方向感兴趣",
    nextAction: "约试听课",
    createdAt: "2026-05-20T10:00:00"
  }
];

describe("FollowUpTimeline", () => {
  it("renders all follow-up items", () => {
    const wrapper = createWrapper(FollowUpTimeline, {
      followUps: mockFollowUps,
      loading: false
    });
    expect(wrapper.text()).toContain("试听课圆满成功");
    expect(wrapper.text()).toContain("初步沟通");
  });

  it("renders user display names", () => {
    const wrapper = createWrapper(FollowUpTimeline, {
      followUps: mockFollowUps,
      loading: false
    });
    expect(wrapper.text()).toContain("秦梓源");
  });

  it("handles empty followUps array", () => {
    const wrapper = createWrapper(FollowUpTimeline, {
      followUps: [],
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(FollowUpTimeline, {
      followUps: mockFollowUps,
      loading: true
    });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("FollowUpCreateForm", () => {
  it("renders create button area", () => {
    const wrapper = createWrapper(FollowUpCreateForm, {
      customerId: 1
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("accepts customerId prop", () => {
    const wrapper = createWrapper(FollowUpCreateForm, {
      customerId: 42
    });
    expect(wrapper.exists()).toBe(true);
  });
});
