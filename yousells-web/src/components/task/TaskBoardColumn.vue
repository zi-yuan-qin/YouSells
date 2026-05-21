<script setup lang="ts">
import type { TaskBoardItem, TaskBoardColumn } from "@/types/task";
import TaskBoardCard from "./TaskBoardCard.vue";

defineProps<{
  column: TaskBoardColumn;
  loading: boolean;
}>();

const emit = defineEmits<{
  "task-click": [task: TaskBoardItem];
  "task-status-change": [task: TaskBoardItem, newStatus: string];
}>();
</script>

<template>
  <div class="board-column" v-loading="loading">
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
  background: #f8fbff;
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
  color: #12213d;
}

.board-column__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.board-column__empty {
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  padding: 24px 0;
}
</style>
