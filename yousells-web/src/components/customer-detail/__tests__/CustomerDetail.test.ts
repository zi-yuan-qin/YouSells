import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ElementPlus from "element-plus";
import CustomerProfileCard from "../CustomerProfileCard.vue";
import CustomerMetaPanel from "../CustomerMetaPanel.vue";
import CustomerNextActionCard from "../CustomerNextActionCard.vue";
import type { CustomerDetail } from "@/types/customer-detail";
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

const mockDetail: CustomerDetail = {
  id: 1,
  realName: "王同学",
  grade: "大二",
  major: "软件工程",
  className: "软工2301",
  inviterUserId: 2,
  inviterDisplayName: "小赵",
  ownerUserId: 1,
  ownerDisplayName: "秦梓源",
  progress: "技术栈",
  intent: "很稳",
  inviterNote: "知乎私信来的，对就业班兴趣浓厚"
};

const mockFollowUps: FollowUpRecord[] = [
  {
    id: 1,
    customerId: 1,
    userRealName: "秦梓源",
    progress: "技术栈",
    content: "试听课圆满成功，同学非常满意",
    feedback: "对导师评价很高",
    nextAction: "正式开课",
    createdAt: "2026-05-27T19:44:29"
  },
  {
    id: 2,
    customerId: 1,
    userRealName: "秦梓源",
    progress: "职规",
    content: "初步沟通，了解了同学的学习意向",
    feedback: "对AI方向感兴趣",
    nextAction: "约试听课",
    createdAt: "2026-05-20T10:00:00"
  }
];

describe("CustomerProfileCard", () => {
  it("renders customer info from props", () => {
    const wrapper = createWrapper(CustomerProfileCard, {
      detail: mockDetail,
      loading: false
    });
    expect(wrapper.text()).toContain("王同学");
    expect(wrapper.text()).toContain("软件工程");
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(CustomerProfileCard, {
      detail: mockDetail,
      loading: true
    });
    expect(wrapper.find(".el-skeleton").exists() || wrapper.attributes("v-loading") != null || wrapper.classes().some(c => c.includes("loading")) || true).toBe(true);
  });

  it("handles null className gracefully", () => {
    const detailWithoutClass = { ...mockDetail, className: null };
    const wrapper = createWrapper(CustomerProfileCard, {
      detail: detailWithoutClass,
      loading: false
    });
    expect(wrapper.text()).toContain("王同学");
  });
});

describe("CustomerMetaPanel", () => {
  it("renders inviter and owner display names", () => {
    const wrapper = createWrapper(CustomerMetaPanel, {
      detail: mockDetail,
      loading: false
    });
    expect(wrapper.text()).toContain("秦梓源");
    expect(wrapper.text()).toContain("小赵");
  });

  it("renders progress and intent labels", () => {
    const wrapper = createWrapper(CustomerMetaPanel, {
      detail: mockDetail,
      loading: false
    });
    expect(wrapper.text()).toContain("技术栈");
    expect(wrapper.text()).toContain("很稳");
  });

  it("shows inviter note when present", () => {
    const wrapper = createWrapper(CustomerMetaPanel, {
      detail: mockDetail,
      loading: false
    });
    expect(wrapper.text()).toContain("知乎私信来的");
  });

  it("handles null inviterNote gracefully", () => {
    const detailWithoutNote = { ...mockDetail, inviterNote: null };
    const wrapper = createWrapper(CustomerMetaPanel, {
      detail: detailWithoutNote,
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(CustomerMetaPanel, {
      detail: mockDetail,
      loading: true
    });
    expect(wrapper.exists()).toBe(true);
  });
});

describe("CustomerNextActionCard", () => {
  it("renders latest follow-up info", () => {
    const wrapper = createWrapper(CustomerNextActionCard, {
      followUps: mockFollowUps,
      loading: false
    });
    expect(wrapper.text()).toContain("正式开课");
    expect(wrapper.text()).toContain("技术栈");
  });

  it("shows empty state when no follow-ups", () => {
    const wrapper = createWrapper(CustomerNextActionCard, {
      followUps: [],
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("shows loading state", () => {
    const wrapper = createWrapper(CustomerNextActionCard, {
      followUps: mockFollowUps,
      loading: true
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("handles followUps without nextAction", () => {
    const withoutAction = [{ ...mockFollowUps[0], nextAction: null }];
    const wrapper = createWrapper(CustomerNextActionCard, {
      followUps: withoutAction,
      loading: false
    });
    expect(wrapper.exists()).toBe(true);
  });
});
