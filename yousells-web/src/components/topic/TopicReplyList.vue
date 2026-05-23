<script setup lang="ts">
import type { TopicReply } from "@/types/topic";
import { formatDate } from "@/utils/format";

const props = defineProps<{
  replies: TopicReply[];
  solved: boolean;
  isAuthor: boolean;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "markSolution", replyId: number): void;
}>();
</script>

<template>
  <div class="reply-list">
    <div v-if="loading" class="reply-list__loading">加载中...</div>
    <div v-else-if="replies.length === 0" class="reply-list__empty">暂无回答</div>
    <div v-else>
      <div
        v-for="reply in replies"
        :key="reply.id"
        class="reply-item"
      >
        <div class="reply-item__avatar">
          <el-avatar :size="36">{{ reply.userName?.[0] || "?" }}</el-avatar>
        </div>
        <div class="reply-item__body">
          <div class="reply-item__header">
            <span class="reply-item__name">{{ reply.userName }}</span>
            <span class="reply-item__time">{{ formatDate(reply.createdAt) }}</span>
            <el-tag
              v-if="reply.isSolution"
              type="warning"
              size="small"
            >
              ⭐ 最佳方案
            </el-tag>
            <el-button
              v-if="isAuthor && !solved"
              size="small"
              text
              type="primary"
              @click="emit('markSolution', reply.id)"
            >
              标记为最佳方案
            </el-button>
          </div>
          <div class="reply-item__content">{{ reply.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reply-list__loading,
.reply-list__empty {
  padding: 24px 0;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-item__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.reply-item__name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.reply-item__time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.reply-item__content {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
