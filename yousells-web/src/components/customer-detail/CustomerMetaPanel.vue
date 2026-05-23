<script setup lang="ts">
import type { CustomerDetail } from "@/types/customer-detail";

defineProps<{
  detail: CustomerDetail;
  loading: boolean;
}>();

const emit = defineEmits<{
  "tags-updated": [];
}>();
</script>

<template>
  <div class="meta-panel" v-loading="loading">
    <div class="meta-section">
      <div class="meta-section__label">归属信息</div>
      <div class="meta-section__body">
        <div class="meta-row">
          <span class="meta-row__key">邀约人</span>
          <span class="meta-row__value">{{ detail.inviterDisplayName || "暂无" }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">负责人</span>
          <span class="meta-row__value">{{ detail.ownerDisplayName }}</span>
        </div>
      </div>
    </div>

    <div class="meta-section">
      <div class="meta-section__label">当前状态</div>
      <div class="meta-section__body">
        <div class="meta-row">
          <span class="meta-row__key">进度</span>
          <span class="meta-row__value">{{ detail.progress }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">意向</span>
          <span class="meta-row__value">{{ detail.intent }}</span>
        </div>
      </div>
    </div>

    <div class="meta-section" v-if="detail.inviterNote">
      <div class="meta-section__label">邀约备注</div>
      <div class="meta-section__body">
        <p class="inviter-note">{{ detail.inviterNote }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.meta-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.meta-section__label {
  font-weight: 700;
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}

.meta-section__body {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-row {
  width: 100%;
  display: flex;
  gap: 12px;
  padding: 6px 0;
  font-size: 13px;
}

.meta-row__key {
  color: var(--color-text-secondary);
  min-width: 70px;
}

.meta-row__value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.inviter-note {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  width: 100%;
}
</style>
