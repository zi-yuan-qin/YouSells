// 客户阶段 → 中文
const STAGE_LABEL_MAP: Record<string, string> = {
  UNTOUCHED: "未接触",
  ADDED_CONTACT: "已加好友",
  FIRST_COMMUNICATION: "首轮沟通",
  JOINED_GROUP: "已进群",
  NURTURING: "培育中",
  HIGH_INTENT: "高意向",
  PENDING_CLOSE: "待成交",
  CONVERTED: "已成交",
  PAUSED: "暂停",
  TRANSFER_TO_EXAM: "转考"
};

export function stageLabel(code: string): string {
  return STAGE_LABEL_MAP[code] ?? code;
}

// 意向等级 → 中文
const INTENT_LABEL_MAP: Record<string, string> = {
  A: "A级·高意向",
  B: "B级·中高意向",
  C: "C级·一般意向",
  D: "D级·低意向"
};

export function intentLabel(code: string): string {
  return INTENT_LABEL_MAP[code] ?? code;
}

// 任务状态 → 中文
const TASK_STATUS_LABEL_MAP: Record<string, string> = {
  TODO: "待开始",
  IN_PROGRESS: "进行中",
  DONE: "已完成",
  BLOCKED: "阻塞"
};

export function taskStatusLabel(code: string): string {
  return TASK_STATUS_LABEL_MAP[code] ?? code;
}

// 跟进类型 → 中文
const FOLLOW_TYPE_LABEL_MAP: Record<string, string> = {
  ONLINE_CHAT: "在线沟通",
  PHONE_CALL: "电话沟通",
  INSTANT_MESSAGE: "即时通讯",
  OFFLINE_MEETING: "线下见面",
  EMAIL: "邮件",
  OTHER: "其他"
};

export function followTypeLabel(code: string): string {
  return FOLLOW_TYPE_LABEL_MAP[code] ?? code;
}
