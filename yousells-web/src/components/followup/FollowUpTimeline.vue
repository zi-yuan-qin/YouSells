<script setup lang="ts">
import { computed } from "vue";
import type { FollowUpRecord } from "@/types/followup";
import { datetime } from "@/utils/format";

const props = defineProps<{
  followUps: FollowUpRecord[];
  loading: boolean;
}>();

const progressColorMap: Record<string, string> = {
  "职规": "#3b82f6",
  "技术栈": "#8b5cf6",
  "课程": "#10b981"
};

const progressBgMap: Record<string, string> = {
  "职规": "rgba(59, 130, 246, 0.08)",
  "技术栈": "rgba(139, 92, 246, 0.08)",
  "课程": "rgba(16, 185, 129, 0.08)"
};

function avatarChar(name: string) {
  return name ? name.charAt(0) : "?";
}

function avatarColor(name: string) {
  if (!name) return "#94a3b8";
  const colors = ["#2563eb", "#7c3aed", "#db2777", "#ea580c", "#059669", "#0891b2"];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return colors[Math.abs(hash) % colors.length];
}

const sortedItems = computed(() =>
  [...props.followUps].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
);
</script>

<template>
  <div class="followup-timeline" v-loading="loading">
    <div v-if="followUps.length === 0 && !loading" class="followup-timeline__empty">
      <el-empty description="暂无跟进记录" />
    </div>

    <div v-else class="timeline">
      <div
        v-for="(item, index) in sortedItems"
        :key="item.id"
        class="timeline-item"
        :class="{ 'timeline-item--latest': index === 0 }"
      >
        <div class="timeline-item__left">
          <div
            class="timeline-item__dot"
            :style="{ background: progressColorMap[item.progress] || '#94a3b8' }"
            :class="{ 'timeline-item__dot--pulse': index === 0 }"
          />
          <div v-if="index !== sortedItems.length - 1" class="timeline-item__line" />
        </div>

        <div class="timeline-item__card">
          <div class="timeline-item__header">
            <div class="timeline-item__meta">
              <span
                class="timeline-item__progress"
                :style="{
                  color: progressColorMap[item.progress] || 'var(--color-text-muted)',
                  background: progressBgMap[item.progress] || 'var(--color-bg-hover)'
                }"
              >
                {{ item.progress }}
              </span>
              <span class="timeline-item__time">{{ datetime(item.createdAt) }}</span>
            </div>
            <div class="timeline-item__user">
              <div
                class="timeline-item__avatar"
                :style="{ background: avatarColor(item.userRealName) }"
              >
                {{ avatarChar(item.userRealName) }}
              </div>
              <span class="timeline-item__user-name">{{ item.userRealName }}</span>
            </div>
          </div>

          <div class="timeline-item__body">
            <div class="timeline-item__field">
              <span class="timeline-item__field-label">沟通内容</span>
              <p class="timeline-item__field-text">{{ item.content }}</p>
            </div>
            <div v-if="item.feedback" class="timeline-item__field">
              <span class="timeline-item__field-label">学生反馈</span>
              <p class="timeline-item__field-text">{{ item.feedback }}</p>
            </div>
            <div v-if="item.nextAction" class="timeline-item__field">
              <span class="timeline-item__field-label">下一步</span>
              <p class="timeline-item__field-text timeline-item__field-text--action">
                {{ item.nextAction }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.followup-timeline {
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.followup-timeline__empty {
  text-align: center;
  padding: 32px 0;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-item {
  display: flex;
  gap: 14px;
  padding: 8px 0;
}

.timeline-item__left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
  flex-shrink: 0;
  padding-top: 14px;
}

.timeline-item__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--color-bg-card);
  box-shadow: 0 0 0 2px currentColor;
  flex-shrink: 0;
}

.timeline-item__dot--pulse {
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 2px currentColor, 0 0 0 0 currentColor; }
  50% { box-shadow: 0 0 0 2px currentColor, 0 0 0 6px transparent; }
}

.timeline-item__line {
  flex: 1;
  width: 2px;
  background: linear-gradient(to bottom, var(--color-border), transparent);
  margin-top: 8px;
  min-height: 40px;
}

.timeline-item__card {
  flex: 1;
  background: var(--color-bg-surface);
  border-radius: 14px;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  transition: box-shadow 0.2s, transform 0.2s;
}

.timeline-item__card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-1px);
}

.timeline-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.timeline-item__meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.timeline-item__progress {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
}

.timeline-item__time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.timeline-item__user {
  display: flex;
  align-items: center;
  gap: 6px;
}

.timeline-item__avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.timeline-item__user-name {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.timeline-item__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-item__field {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.timeline-item__field-label {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 500;
}

.timeline-item__field-text {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-primary);
  line-height: 1.6;
}

.timeline-item__field-text--action {
  color: var(--color-primary);
  background: rgba(37, 99, 235, 0.04);
  padding: 6px 10px;
  border-radius: 8px;
  display: inline-block;
}
</style>
