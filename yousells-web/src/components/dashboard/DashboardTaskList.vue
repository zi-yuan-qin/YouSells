<script setup lang="ts">
import type { DashboardTaskReminder } from "@/types/dashboard";
import { taskStatusLabel } from "@/constants/stage";
import { friendlyDate } from "@/utils/format";

defineProps<{
  tasks: DashboardTaskReminder[];
  loading: boolean;
}>();
</script>

<template>
  <div class="list-card">
    <h3>今日公共安排</h3>
    <p v-if="loading" class="list-card__placeholder">加载中...</p>
    <p v-else-if="tasks.length === 0" class="list-card__placeholder">暂无今日安排</p>
    <ul v-else>
      <li v-for="task in tasks" :key="task.taskId">
        {{ task.taskTitle }}｜{{ taskStatusLabel(task.status) }}｜{{ task.ownerDisplayName }}｜{{ friendlyDate(task.dueAt) }}
      </li>
    </ul>
  </div>
</template>
