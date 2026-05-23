<script setup lang="ts">
import { computed } from "vue";
import { ArrowUp, ArrowDown, Minus } from "@element-plus/icons-vue";

const props = defineProps<{
  trend: "up" | "down" | "flat";
  value?: string;
}>();

const typeMap = {
  up: "success",
  down: "danger",
  flat: "info"
} as const;

const iconMap = {
  up: ArrowUp,
  down: ArrowDown,
  flat: Minus
};

const tagType = computed(() => typeMap[props.trend]);
const icon = computed(() => iconMap[props.trend]);
</script>

<template>
  <el-tag :type="tagType" size="small" class="trend-tag">
    <el-icon><component :is="icon" /></el-icon>
    <span v-if="value">{{ value }}</span>
  </el-tag>
</template>

<style scoped>
.trend-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
}

.trend-tag .el-icon {
  font-size: 12px;
}
</style>
