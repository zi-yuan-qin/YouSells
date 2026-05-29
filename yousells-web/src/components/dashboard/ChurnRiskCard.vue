<script setup lang="ts">
import { Warning, Refresh } from "@element-plus/icons-vue";
import type { ChurnRiskResponse } from "@/types/churn";
import ChurnRiskItem from "./ChurnRiskItem.vue";

defineProps<{
  risks: ChurnRiskResponse | null;
  loading: boolean;
  evaluating: boolean;
}>();

const emit = defineEmits<{
  evaluate: [];
  viewCustomer: [customerId: number];
}>();

function formatEvalTime(dateStr: string | null): string {
  if (!dateStr) return "";
  try {
    const d = new Date(dateStr);
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  } catch {
    return dateStr;
  }
}
</script>

<template>
  <div class="churn-card" v-loading="loading">
    <div class="churn-card__header">
      <div class="churn-card__title">
        <el-icon color="#f56c6c"><Warning /></el-icon>
        <span>流失风险预警</span>
        <el-tag
          v-if="risks && risks.highRisk.length > 0"
          size="small"
          type="danger"
          effect="dark"
        >
          {{ risks.highRisk.length }} 高风险
        </el-tag>
        <el-tag
          v-if="risks && risks.mediumRisk.length > 0"
          size="small"
          type="warning"
          effect="dark"
        >
          {{ risks.mediumRisk.length }} 关注
        </el-tag>
      </div>
      <el-button
        text
        size="small"
        :loading="evaluating"
        @click="emit('evaluate')"
      >
        <el-icon><Refresh /></el-icon> 刷新评估
      </el-button>
    </div>

    <div v-if="!loading && (!risks || risks.total === 0)" class="churn-card__empty">
      <span class="churn-card__empty-icon">✅</span>
      <p>所有客户状态健康</p>
      <p class="churn-card__empty-sub">暂无流失风险</p>
    </div>

    <div v-else-if="risks">
      <div v-if="risks.highRisk.length > 0" class="churn-card__section churn-card__section--high">
        <div class="churn-card__section-title">高风险客户 ({{ risks.highRisk.length }})</div>
        <ChurnRiskItem
          v-for="item in risks.highRisk"
          :key="item.customerId"
          :item="item"
          @click="emit('viewCustomer', item.customerId)"
        />
      </div>

      <div v-if="risks.mediumRisk.length > 0" class="churn-card__section churn-card__section--medium">
        <div class="churn-card__section-title">需关注客户 ({{ risks.mediumRisk.length }})</div>
        <ChurnRiskItem
          v-for="item in risks.mediumRisk"
          :key="item.customerId"
          :item="item"
          @click="emit('viewCustomer', item.customerId)"
        />
      </div>

      <div v-if="risks.evaluatedAt" class="churn-card__footer">
        评估时间：{{ formatEvalTime(risks.evaluatedAt) }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.churn-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  box-shadow: var(--shadow-card);
}

.churn-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.churn-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.churn-card__empty {
  text-align: center;
  padding: 24px 0;
  color: var(--color-text-secondary);
}

.churn-card__empty-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.churn-card__empty p {
  margin: 0;
  font-size: 14px;
}

.churn-card__empty-sub {
  font-size: 12px;
  color: var(--color-text-disabled);
  margin-top: 4px;
}

.churn-card__section {
  margin-bottom: 12px;
}

.churn-card__section--high {
  border-left: 3px solid var(--color-danger);
  padding-left: 12px;
}

.churn-card__section--medium {
  border-left: 3px solid var(--color-warning);
  padding-left: 12px;
}

.churn-card__section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.churn-card__footer {
  font-size: 11px;
  color: var(--color-text-disabled);
  text-align: right;
  margin-top: 8px;
}
</style>
