<script setup lang="ts">
import { computed } from "vue";
import { ArrowUp, ArrowDown, Minus } from "@element-plus/icons-vue";

const props = defineProps<{
  title: string;
  value: string | number;
  trend?: "up" | "down" | "flat" | null;
  change?: string;
  icon?: unknown;
  color?: "primary" | "success" | "warning" | "danger" | "info";
}>();

const colorMap: Record<string, string> = {
  primary: "#2563eb",
  success: "#10b981",
  warning: "#f59e0b",
  danger: "#ef4444",
  info: "#3b82f6"
};

const trendColor = computed(() => {
  if (props.trend === "up") return "var(--color-success)";
  if (props.trend === "down") return "var(--color-danger)";
  return "var(--color-text-muted)";
});

const trendIcon = computed(() => {
  if (props.trend === "up") return ArrowUp;
  if (props.trend === "down") return ArrowDown;
  return Minus;
});

const accentColor = computed(() =>
  props.color ? colorMap[props.color] || colorMap.primary : colorMap.primary
);
</script>

<template>
  <div class="stat-card">
    <div class="stat-card__header">
      <span class="stat-card__title">{{ title }}</span>
      <el-icon
        v-if="icon"
        class="stat-card__icon"
        :style="{ color: accentColor }"
      >
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="stat-card__value">{{ value }}</div>
    <div v-if="change" class="stat-card__trend" :style="{ color: trendColor }">
      <el-icon><component :is="trendIcon" /></el-icon>
      <span>{{ change }}</span>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-card);
  transition: box-shadow var(--duration-base) var(--ease-out-expo),
    transform var(--duration-base) var(--ease-out-expo);
}

.stat-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-2px);
}

.stat-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-sm);
}

.stat-card__title {
  font: var(--font-caption);
  color: var(--color-text-secondary);
  font-weight: 500;
}

.stat-card__icon {
  font-size: 20px;
  opacity: 0.8;
}

.stat-card__value {
  font: var(--font-display);
  color: var(--color-text-primary);
  margin-bottom: var(--space-xs);
}

.stat-card__trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font: var(--font-caption);
  font-weight: 500;
}
</style>
