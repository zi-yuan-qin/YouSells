<script setup lang="ts">
import type { ScriptItem } from "@/types/script";

defineProps<{
  visible: boolean;
  script: ScriptItem | null;
  loading: boolean;
}>();

const emit = defineEmits<{
  close: [];
  edit: [script: ScriptItem];
}>();
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="话术详情"
    size="480px"
    @update:model-value="emit('close')"
  >
    <template v-if="script">
      <div class="preview-section" v-loading="loading">
        <div class="preview-row">
          <span class="preview-row__label">标题</span>
          <span class="preview-row__value preview-title">{{ script.title }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-row__label">分类</span>
          <span class="preview-row__value">{{ script.categoryName }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-row__label">适用场景</span>
          <span class="preview-row__value">{{ script.applicableScene || "—" }}</span>
        </div>
        <div class="preview-row">
          <span class="preview-row__label">状态</span>
          <el-tag :type="script.status === '启用' ? 'success' : 'info'" size="small">
            {{ script.status }}
          </el-tag>
        </div>
        <div class="preview-row">
          <span class="preview-row__label">内容</span>
        </div>
        <div class="preview-content">{{ script.content }}</div>
        <div class="preview-row">
          <span class="preview-row__label">更新时间</span>
          <span class="preview-row__value">{{ script.updatedAt }}</span>
        </div>
      </div>
    </template>
    <template #footer>
      <el-button @click="emit('close')">关闭</el-button>
      <el-button type="primary" v-if="script" @click="emit('edit', script!)">
        编辑
      </el-button>
    </template>
  </el-drawer>
</template>

<style scoped>
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-row__label {
  color: #94a3b8;
  font-size: 13px;
  min-width: 70px;
  flex-shrink: 0;
}

.preview-row__value {
  font-weight: 500;
  color: #172033;
}

.preview-title {
  font-size: 18px;
  font-weight: 700;
}

.preview-content {
  background: #f8fbff;
  border-radius: 12px;
  padding: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #42506a;
  font-size: 14px;
}
</style>
