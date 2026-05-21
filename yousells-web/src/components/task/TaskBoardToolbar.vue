<script setup lang="ts">
defineProps<{
  loading: boolean;
}>();

defineEmits<{
  refresh: [];
  "create-task": [];
}>();
</script>

<template>
  <div class="board-toolbar">
    <div class="board-toolbar__actions">
      <el-button type="primary" @click="$emit('create-task')">新增任务</el-button>
      <el-button :loading="loading" @click="$emit('refresh')">刷新看板</el-button>
    </div>
  </div>
</template>

<style scoped>
.board-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.board-toolbar__actions {
  display: flex;
  gap: 8px;
}
</style>
