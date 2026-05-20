<script setup lang="ts">
import type { DashboardTaskReminder } from "@/types/dashboard";

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
        {{ task.taskTitle }}｜{{ task.status }}｜{{ task.ownerDisplayName }}｜截止 {{ task.dueAt }}
      </li>
    </ul>
  </div>
</template>
