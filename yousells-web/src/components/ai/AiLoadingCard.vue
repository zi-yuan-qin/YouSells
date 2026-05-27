<script setup lang="ts">
import { WarningFilled, MagicStick } from '@element-plus/icons-vue'

defineProps<{
  loading?: boolean
  error?: string | null
  result?: string | null
}>()

const emit = defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="ai-card" :class="{ 'ai-card--loading': loading, 'ai-card--error': error, 'ai-card--result': result && !loading }">
    <!-- 思考中 -->
    <div v-if="loading" class="ai-card__thinking">
      <span class="thinking-dot" />
      <span class="thinking-dot" />
      <span class="thinking-dot" />
      <span class="thinking-text">AI 思考中...</span>
    </div>

    <!-- 错误 -->
    <div v-else-if="error" class="ai-card__error">
      <el-icon :size="20"><WarningFilled /></el-icon>
      <span>{{ error }}</span>
      <el-button size="small" type="primary" text @click="emit('retry')">重试</el-button>
    </div>

    <!-- 结果 -->
    <div v-else-if="result" class="ai-card__result">
      <div class="ai-card__result-header">
        <el-icon :size="16" color="var(--el-color-primary)"><MagicStick /></el-icon>
        <span>AI 分析</span>
      </div>
      <div class="ai-card__result-body" v-text="result" />
    </div>

    <!-- 空（初始态） -->
    <div v-else class="ai-card__idle">
      <el-icon :size="18" color="var(--el-color-info)"><MagicStick /></el-icon>
      <span>AI 分析就绪</span>
    </div>
  </div>
</template>

<style scoped>
.ai-card {
  padding: 16px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
  min-height: 60px;
  display: flex;
  align-items: center;
  transition: border-color 0.3s;
}

.ai-card--loading {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.ai-card--error {
  border-color: var(--el-color-danger-light-5);
  background: var(--el-color-danger-light-9);
}

.ai-card--result {
  border-color: var(--el-color-primary-light-5);
}

/* 思考动画 */
.ai-card__thinking {
  display: flex;
  align-items: center;
  gap: 4px;
}

.thinking-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.thinking-dot:nth-child(1) { animation-delay: -0.32s; }
.thinking-dot:nth-child(2) { animation-delay: -0.16s; }
.thinking-dot:nth-child(3) { animation-delay: 0s; }

.thinking-text {
  margin-left: 8px;
  font-size: 14px;
  color: var(--el-color-primary);
}

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 错误 */
.ai-card__error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-color-danger);
  font-size: 14px;
}

/* 结果 */
.ai-card__result {
  width: 100%;
}

.ai-card__result-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.ai-card__result-body {
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
}

/* 初始态 */
.ai-card__idle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
</style>
