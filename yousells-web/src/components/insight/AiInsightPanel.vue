<script setup lang="ts">
import { computed } from "vue";
import { MagicStick, WarningFilled, Refresh, Timer } from "@element-plus/icons-vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import IntentTrendBadge from "./IntentTrendBadge.vue";
import KeyConcernTags from "./KeyConcernTags.vue";
import ConversionGauge from "./ConversionGauge.vue";
import { datetime } from "@/utils/format";
import type { AiInsight } from "@/types/ai-insight";

const props = defineProps<{
  insight: AiInsight | null;
  loading: boolean;
  error: boolean;
}>();

defineEmits<{
  refresh: [];
}>();

const isStale = computed(() => {
  if (!props.insight?.generatedAt) return false;
  const generated = new Date(props.insight.generatedAt).getTime();
  const now = Date.now();
  return (now - generated) > 2 * 60 * 60 * 1000;
});

const staleHours = computed(() => {
  if (!props.insight?.generatedAt) return 0;
  const generated = new Date(props.insight.generatedAt).getTime();
  const diff = Date.now() - generated;
  return Math.floor(diff / (60 * 60 * 1000));
});
</script>

<template>
  <div class="ai-insight-panel">
    <div class="panel-header">
      <div class="panel-header__left">
        <el-icon :size="18" color="var(--el-color-primary)"><MagicStick /></el-icon>
        <span class="panel-title">AI 客户洞察</span>
      </div>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="panel-body panel-loading">
      <span class="thinking-dot" />
      <span class="thinking-dot" />
      <span class="thinking-dot" />
      <span class="thinking-text">AI 分析中...</span>
    </div>

    <!-- 错误态 -->
    <div v-else-if="error" class="panel-body panel-error">
      <el-alert type="warning" title="AI 分析暂时不可用" show-icon :closable="false">
        <template #default>
          <el-button size="small" type="warning" text @click="$emit('refresh')">重试</el-button>
        </template>
      </el-alert>
    </div>

    <!-- 无数据态 -->
    <div v-else-if="!insight" class="panel-body panel-empty">
      <EmptyState
        title="暂无 AI 洞察"
        description="点击下方按钮，AI 将分析客户跟进记录并生成洞察报告"
        action-text="开始分析"
        @action="$emit('refresh')"
      />
    </div>

    <!-- 正常 / 过期态 -->
    <div v-else class="panel-body panel-result">
      <div v-if="isStale" class="stale-banner">
        <el-icon><Timer /></el-icon>
        <span>洞察已超过 {{ staleHours }} 小时，建议刷新以获取最新分析</span>
        <el-button size="small" text type="warning" @click="$emit('refresh')">立即刷新</el-button>
      </div>

      <div class="insight-summary">{{ insight.summary }}</div>

      <IntentTrendBadge :trend="insight.intentTrend" :reason="insight.intentTrendReason" />

      <ConversionGauge :probability="insight.conversionProbability" :confidence="insight.conversionConfidence" />

      <KeyConcernTags :concerns="insight.keyConcerns" />

      <div class="insight-field">
        <span class="label">沟通风格</span>
        <el-tag size="default" type="info" effect="plain">{{ insight.communicationStyle }}</el-tag>
      </div>

      <div class="insight-field">
        <span class="label">建议行动</span>
        <p class="suggestion-text">{{ insight.nextActionSuggestion }}</p>
      </div>

      <div class="insight-footer">
        <span class="footer-time">生成于 {{ datetime(insight.generatedAt) }}</span>
        <el-button text type="primary" :icon="Refresh" @click="$emit('refresh')">重新分析</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-insight-panel {
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--radius-lg);
  background: var(--color-bg-card);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--color-bg-page);
}

.panel-header__left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.panel-title {
  font: var(--font-h3);
  color: var(--color-text-primary);
}

.panel-body {
  padding: var(--space-lg);
}

/* Loading */
.panel-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 120px;
  background: var(--el-color-primary-light-9);
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

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.thinking-text {
  margin-left: 8px;
  font: var(--font-body);
  color: var(--el-color-primary);
}

/* Error */
.panel-error {
  min-height: 80px;
  display: flex;
  align-items: center;
}

/* Empty */
.panel-empty {
  min-height: 100px;
}

/* Result */
.stale-banner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-bottom: var(--space-md);
  background: #fef3c7;
  border: 1px solid #f59e0b33;
  border-radius: var(--radius-sm);
  font: var(--font-caption);
  color: #92400e;
}

.insight-summary {
  font: var(--font-body);
  color: var(--color-text-primary);
  line-height: 1.6;
  padding: var(--space-sm) var(--space-md);
  margin-bottom: var(--space-md);
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--el-color-primary);
}

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

.suggestion-text {
  font: var(--font-body);
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.5;
  padding: var(--space-sm) var(--space-md);
  background: var(--color-bg-page);
  border-radius: var(--radius-sm);
}

.insight-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-lg);
  padding-top: var(--space-md);
  border-top: 1px solid var(--el-border-color-lighter);
}

.footer-time {
  font: var(--font-caption);
  color: var(--color-text-muted);
}
</style>
