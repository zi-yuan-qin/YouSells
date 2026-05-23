<script setup lang="ts">
import type { TaskBoardItem } from "@/types/task";
import { MoreFilled } from "@element-plus/icons-vue";

const props = defineProps<{
  item: TaskBoardItem;
}>();

const emit = defineEmits<{
  click: [];
  "status-change": [newStatus: string];
  dragstart: [e: DragEvent];
}>();

function onDragStart(e: DragEvent) {
  e.dataTransfer?.setData("text/plain", String(props.item.id));
  e.dataTransfer!.effectAllowed = "move";
  emit("dragstart", e);
}

const statusOptions = ["待开始", "进行中", "已完成"];

const priorityTypeMap: Record<string, "danger" | "warning" | "info" | ""> = {
  "高": "danger",
  "中": "warning",
  "低": "info"
};
</script>

<template>
  <div
    class="task-card"
    draggable="true"
    @click="$emit('click')"
    @dragstart="onDragStart"
  >
    <div class="task-card__top">
      <span class="task-card__title">{{ item.taskTitle }}</span>
      <div style="display: flex; gap: 6px; align-items: center;">
        <el-tag
          v-if="item.priority"
          :type="priorityTypeMap[item.priority] || 'info'"
          size="small"
          effect="dark"
        >
          {{ item.priority }}
        </el-tag>
        <el-dropdown
          trigger="click"
          @command="(cmd: string) => $emit('status-change', cmd)"
          @click.stop
        >
          <el-icon class="task-card__more"><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-item
              v-for="opt in statusOptions"
              :key="opt"
              :command="opt"
              :disabled="item.status === opt"
            >
              {{ opt }}
            </el-dropdown-item>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="task-card__meta">
      <span class="task-card__owner">{{ item.ownerDisplayName }}</span>
      <span
        v-if="item.dueAt"
        class="task-card__due"
        :class="{ 'task-card__due--overdue': new Date(item.dueAt).getTime() < Date.now() && item.status !== '已完成' }"
      >
        {{ item.dueAt }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.task-card {
  padding: 14px;
  background: var(--color-bg-surface);
  border-radius: 14px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.task-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-1px);
}

.task-card__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
}

.task-card__title {
  font-weight: 600;
  font-size: 14px;
  line-height: 1.5;
}

.task-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-muted);
}

.task-card__due {
  font-size: 12px;
}

.task-card__due--overdue {
  color: var(--color-danger);
  font-weight: 600;
}

.task-card__more {
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 14px;
}

.task-card__more:hover {
  color: var(--color-primary);
}
</style>
