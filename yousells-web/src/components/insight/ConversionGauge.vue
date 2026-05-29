<script setup lang="ts">
import { computed } from "vue";
import { Odometer } from "@element-plus/icons-vue";

const props = defineProps<{
  probability: "高" | "中" | "低";
  confidence: number;
}>();

const tagType = computed(() => {
  switch (props.probability) {
    case "高": return "success";
    case "中": return "warning";
    case "低": return "danger";
    default: return "info";
  }
});

const progressColor = computed(() => {
  switch (props.probability) {
    case "高": return "#10b981";
    case "中": return "#f59e0b";
    case "低": return "#f87171";
    default: return "#3b82f6";
  }
});
</script>

<template>
  <div class="insight-field">
    <span class="label">
      <el-icon><Odometer /></el-icon>
      转化概率
    </span>
    <div class="gauge-row">
      <el-tag :type="tagType" size="default" effect="dark">
        {{ probability }}
      </el-tag>
      <div class="confidence-bar">
        <div class="confidence-track">
          <div
            class="confidence-fill"
            :style="{ width: confidence + '%', background: progressColor }"
          />
        </div>
        <span class="confidence-value">{{ confidence }}%</span>
      </div>
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

.gauge-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.confidence-track {
  flex: 1;
  height: 8px;
  background: var(--color-bg-muted);
  border-radius: 4px;
  overflow: hidden;
}

.confidence-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.confidence-value {
  font: var(--font-h3);
  color: var(--color-text-primary);
  min-width: 40px;
  text-align: right;
}
</style>
