<script setup lang="ts">
import { CollectionTag } from "@element-plus/icons-vue";

defineProps<{
  concerns: string[];
}>();
</script>

<template>
  <div class="insight-field">
    <span class="label">
      <el-icon><CollectionTag /></el-icon>
      关键关注点
    </span>
    <div class="tags-wrap">
      <el-tag
        v-for="(item, idx) in concerns"
        :key="idx"
        size="default"
        effect="plain"
        type="warning"
      >
        {{ item }}
      </el-tag>
      <span v-if="concerns.length === 0" class="empty-hint">暂无数据</span>
    </div>
  </div>
</template>

<style scoped>
.insight-field {
  margin-bottom: var(--space-md);
}

.label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font: var(--font-caption);
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-hint {
  font: var(--font-caption);
  color: var(--color-text-muted);
}
</style>
