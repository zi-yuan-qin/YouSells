<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Goods, ChatLineRound } from "@element-plus/icons-vue";
import type { ReportPlazaItem } from "@/types/report";
import { toggleReportLike } from "@/api/report";
import ReportCommentPanel from "./ReportCommentPanel.vue";

const props = defineProps<{
  item: ReportPlazaItem;
}>();

const emit = defineEmits<{
  (e: "likeToggled", id: number, liked: boolean, delta: number): void;
  (e: "commentAdded", id: number): void;
}>();

const showComments = ref(false);
const likeAnimating = ref(false);

function avatarColor(name: string) {
  if (!name) return "#94a3b8";
  const colors = ["#2563eb", "#7c3aed", "#db2777", "#ea580c", "#059669", "#0891b2", "#4f46e5", "#be123c"];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return colors[Math.abs(hash) % colors.length];
}

async function handleLike() {
  likeAnimating.value = true;
  setTimeout(() => (likeAnimating.value = false), 600);
  try {
    const result = await toggleReportLike(props.item.type, props.item.id);
    emit("likeToggled", props.item.id, result.liked, result.liked ? 1 : -1);
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
  }
}

function getLevelTagType(level: string) {
  const map: Record<string, "success" | "warning" | "info" | "danger"> = {
    T0: "info",
    T1: "success",
    T2: "warning",
    T3: "danger"
  };
  return map[level] ?? "info";
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString("zh-CN", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}
</script>

<template>
  <div class="plaza-card">
    <div class="plaza-card__header">
      <div
        class="plaza-card__avatar"
        :style="{ background: avatarColor(item.userRealName) }"
      >
        {{ item.userRealName.charAt(0) }}
      </div>
      <div class="plaza-card__meta">
        <div class="plaza-card__name">
          {{ item.userRealName }}
          <el-tag size="small" :type="getLevelTagType(item.userLevel)">{{ item.userLevel }}</el-tag>
        </div>
        <div class="plaza-card__time">
          <span v-if="item.type === 'daily'">{{ item.reportDate }} 日报</span>
          <span v-else>{{ item.weekKey }} 周报</span>
          <el-tag v-if="item.edited" size="small" type="info">已编辑</el-tag>
        </div>
      </div>
    </div>

    <div class="plaza-card__body">
      <p class="plaza-card__summary">{{ item.summary }}</p>
      <div v-if="item.issues" class="plaza-card__issues">
        <span class="plaza-card__field-label">遇到的问题</span>
        <p class="plaza-card__field-text">{{ item.issues }}</p>
      </div>
      <div v-if="item.tomorrowPlan" class="plaza-card__plan">
        <span class="plaza-card__field-label">明日计划</span>
        <p class="plaza-card__field-text">{{ item.tomorrowPlan }}</p>
      </div>
      <div v-if="item.nextWeekPlan" class="plaza-card__plan">
        <span class="plaza-card__field-label">下周计划</span>
        <p class="plaza-card__field-text">{{ item.nextWeekPlan }}</p>
      </div>

      <div class="plaza-card__stats">
        <el-tag size="small" type="success">新增客户 {{ item.newCustomerCount }}</el-tag>
        <el-tag size="small" type="warning">跟进 {{ item.followUpCount }}</el-tag>
        <el-tag size="small" type="info">任务完成 {{ item.taskCompletedCount }}</el-tag>
        <el-tag v-if="item.type === 'weekly'" size="small" type="danger">课程达成 {{ item.convertedCount }}</el-tag>
      </div>
    </div>

    <div class="plaza-card__footer">
      <button
        class="plaza-card__action"
        :class="{ 'plaza-card__action--active': item.likedByMe, 'plaza-card__action--animate': likeAnimating }"
        @click="handleLike"
      >
        <el-icon size="16"><Goods /></el-icon>
        <span>{{ item.likedByMe ? "已赞" : "赞" }}</span>
        <span v-if="item.likeCount > 0" class="plaza-card__action-count">{{ item.likeCount }}</span>
      </button>
      <button
        class="plaza-card__action"
        :class="{ 'plaza-card__action--active': showComments }"
        @click="showComments = !showComments"
      >
        <el-icon size="16"><ChatLineRound /></el-icon>
        <span>评论</span>
        <span v-if="item.commentCount > 0" class="plaza-card__action-count">{{ item.commentCount }}</span>
      </button>
      <span class="plaza-card__created">{{ formatDate(item.createdAt) }}</span>
    </div>

    <el-collapse-transition>
      <div v-show="showComments">
        <ReportCommentPanel
          :report-type="item.type"
          :report-id="item.id"
          @comment-added="item.commentCount++"
        />
      </div>
    </el-collapse-transition>
  </div>
</template>

<style scoped>
.plaza-card {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--color-border);
  transition: box-shadow 0.25s cubic-bezier(0.2, 0, 0, 1), transform 0.25s cubic-bezier(0.2, 0, 0, 1);
}

.plaza-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-1px);
}

.plaza-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.plaza-card__avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.plaza-card__meta {
  flex: 1;
}

.plaza-card__name {
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
}

.plaza-card__time {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.plaza-card__body {
  margin-bottom: 14px;
}

.plaza-card__summary {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  margin: 0 0 12px;
  white-space: pre-wrap;
}

.plaza-card__issues,
.plaza-card__plan {
  margin-bottom: 8px;
  padding: 10px 12px;
  background: var(--color-bg-hover);
  border-radius: 10px;
  border: 1px solid var(--color-border);
}

.plaza-card__field-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.plaza-card__field-text {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.plaza-card__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.plaza-card__footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.plaza-card__action {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 20px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, transform 0.1s;
}

.plaza-card__action:hover {
  background: var(--color-bg-hover);
}

.plaza-card__action:active {
  transform: scale(0.96);
}

.plaza-card__action--active {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.plaza-card__action--animate .el-icon {
  animation: heart-beat 0.5s ease-in-out;
}

@keyframes heart-beat {
  0%, 100% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(0.95); }
  75% { transform: scale(1.15); }
}

.plaza-card__action-count {
  font-weight: 600;
  font-size: 12px;
}

.plaza-card__created {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
