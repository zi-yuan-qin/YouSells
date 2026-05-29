<script setup lang="ts">
import type { ChurnRiskItem } from "@/types/churn";

defineProps<{
  item: ChurnRiskItem;
}>();

defineEmits<{
  click: [];
}>();
</script>

<template>
  <div class="churn-risk-item" @click="$emit('click')">
    <div class="churn-risk-item__header">
      <span class="churn-risk-item__name">{{ item.customerName }}</span>
      <el-tag
        :type="item.riskLevel === 'high' ? 'danger' : 'warning'"
        size="small"
        effect="dark"
      >
        {{ item.riskScore }}分
      </el-tag>
      <el-tag
        v-if="item.silentDays > 0"
        size="small"
        type="info"
      >
        沉默{{ item.silentDays }}天
      </el-tag>
    </div>
    <div v-if="item.riskFactors.length > 0" class="churn-risk-item__factors">
      <el-tag
        v-for="factor in item.riskFactors"
        :key="factor"
        size="small"
        type="info"
        effect="plain"
      >
        {{ factor }}
      </el-tag>
    </div>
    <div v-if="item.suggestion" class="churn-risk-item__suggestion">
      {{ item.suggestion }}
    </div>
  </div>
</template>

<style scoped>
.churn-risk-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background var(--transition-base);
}
.churn-risk-item:last-child {
  border-bottom: none;
}
.churn-risk-item:hover {
  background: var(--color-bg-hover);
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: var(--radius-sm);
}
.churn-risk-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.churn-risk-item__name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}
.churn-risk-item__factors {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}
.churn-risk-item__suggestion {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
