<script setup lang="ts">
import type { FollowUpRecord } from "@/types/followup";
import { relativeDate } from "@/utils/format";

defineProps<{
  followUps: FollowUpRecord[];
  loading: boolean;
}>();
</script>

<template>
  <div class="next-action-card" v-loading="loading">
    <h3 class="page-section__title" style="font-size: 18px; margin: 0 0 12px">
      最新跟进摘要
    </h3>

    <div v-if="followUps.length === 0" class="next-action-empty">
      暂无跟进记录
    </div>
    <div v-else class="next-action-summary">
      <div class="next-action-row">
        <span class="next-action-row__label">最后沟通</span>
        <span class="next-action-row__value">{{ relativeDate(followUps[0].createdAt) }}</span>
      </div>
      <div class="next-action-row">
        <span class="next-action-row__label">当前进度</span>
        <span class="next-action-row__value">{{ followUps[0].progress }}</span>
      </div>
      <div class="next-action-row" v-if="followUps[0].nextAction">
        <span class="next-action-row__label">下一步</span>
        <span class="next-action-row__value">{{ followUps[0].nextAction }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.next-action-card {
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.next-action-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
  padding: 6px 0;
}

.next-action-row__label {
  color: var(--color-text-secondary);
  min-width: 80px;
}

.next-action-row__value {
  color: var(--color-text-primary);
  font-weight: 600;
}

.next-action-empty {
  color: var(--color-text-muted);
  font-size: 13px;
}
</style>
