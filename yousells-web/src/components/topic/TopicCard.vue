<script setup lang="ts">
import type { TopicListItem } from "@/types/topic";
import { formatDate } from "@/utils/format";

const props = defineProps<{
  topic: TopicListItem;
}>();

const emit = defineEmits<{
  (e: "click", topicId: number): void;
}>();

function categoryTagType(category: string): "" | "success" | "warning" | "info" | "danger" {
  const map: Record<string, "" | "success" | "warning" | "info" | "danger"> = {
    邀约: "success",
    沟通: "warning",
    跟进: "info",
    转化: "danger",
    其他: ""
  };
  return map[category] || "";
}
</script>

<template>
  <div class="topic-card" @click="emit('click', props.topic.id)">
    <div class="topic-card__header">
      <h3 class="topic-card__title">{{ props.topic.title }}</h3>
      <el-tag v-if="props.topic.solved" type="success" size="small">已解决</el-tag>
    </div>
    <div class="topic-card__meta">
      <el-tag size="small" :type="categoryTagType(props.topic.category)">
        {{ props.topic.category }}
      </el-tag>
      <span class="meta-item">{{ props.topic.authorName }}</span>
      <span class="meta-item">{{ props.topic.replyCount }} 个回答</span>
      <span class="meta-item">{{ formatDate(props.topic.createdAt) }}</span>
    </div>
  </div>
</template>

<style scoped>
.topic-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.topic-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-1px);
}

.topic-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.topic-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-card__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 13px;
  color: var(--color-text-muted);
}
</style>
