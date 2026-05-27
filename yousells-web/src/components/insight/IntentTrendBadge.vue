<script setup lang="ts">
import { computed } from "vue";
import { TrendCharts, ArrowUp, ArrowDown, Minus } from "@element-plus/icons-vue";

const props = defineProps<{
  trend: "上升" | "平稳" | "下降";
  reason: string;
}>();

const tagType = computed(() => {
  switch (props.trend) {
    case "上升": return "success";
    case "平稳": return "info";
    case "下降": return "danger";
    default: return "info";
  }
});

const icon = computed(() => {
  switch (props.trend) {
    case "上升": return ArrowUp;
    case "平稳": return Minus;
    case "下降": return ArrowDown;
    default: return Minus;
  }
});
</script>

<template>
  <div class="insight-field">
    <span class="label">
      <el-icon><TrendCharts /></el-icon>
      意向趋势
    </span>
    <div class="field-content">
      <el-tag :type="tagType" size="default" effect="plain">
        <el-icon style="margin-right: 2px"><component :is="icon" /></el-icon>
        {{ trend }}
      </el-tag>
      <p class="field-reason">{{ reason }}</p>
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

.field-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-reason {
  font: var(--font-body);
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.5;
}
</style>
