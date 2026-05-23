<script setup lang="ts">
import type { TaskBoardItem, TaskBoardColumn } from "@/types/task";
import TaskBoardCard from "./TaskBoardCard.vue";

const props = defineProps<{
  column: TaskBoardColumn;
  loading: boolean;
}>();

const emit = defineEmits<{
  "task-click": [task: TaskBoardItem];
  "task-status-change": [task: TaskBoardItem, newStatus: string];
}>();

function onDragOver(e: DragEvent) {
  e.preventDefault();
}

function onDrop(e: DragEvent) {
  e.preventDefault();
  const taskId = e.dataTransfer?.getData("text/plain");
  if (taskId) {
    const item = props.column.items.find(i => i.id === Number(taskId));
    if (item && item.status !== props.column.status) {
      emit("task-status-change", item, props.column.status);
    }
  }
}
</script>

<template>
  <div
    class="board-column"
    v-loading="loading"
    @dragover="onDragOver"
    @drop="onDrop"
  >
    <div class="board-column__header">
      <span class="board-column__title">{{ column.title }}</span>
      <el-badge :value="column.items.length" type="primary" />
    </div>
    <div class="board-column__body">
      <template v-if="column.items.length > 0">
        <TaskBoardCard
          v-for="item in column.items"
          :key="item.id"
          :item="item"
          @click="emit('task-click', item)"
          @status-change="emit('task-status-change', item, $event)"
        />
      </template>
      <div v-else class="board-column__empty">
        暂无任务
      </div>
    </div>
  </div>
</template>

<style scoped>
.board-column {
  min-height: 200px;
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.board-column__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.board-column__title {
  font-weight: 700;
  font-size: 15px;
  color: var(--color-text-primary);
}

.board-column__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.board-column__empty {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 24px 0;
}
</style>
