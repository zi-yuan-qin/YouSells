<script setup lang="ts">
import type { FollowUpRecord } from "@/types/followup";

defineProps<{
  followUps: FollowUpRecord[];
  loading: boolean;
}>();

function followTypeTag(type: string): "" | "success" | "warning" | "info" | "danger" {
  const map: Record<string, "" | "success" | "warning" | "info" | "danger"> = {
    "电话": "success",
    "微信": "info",
    "面谈": "warning",
    "邮件": "",
    "其他": "info"
  };
  return map[type] || "";
}
</script>

<template>
  <div class="followup-timeline" v-loading="loading">
    <div v-if="followUps.length === 0 && !loading" class="followup-timeline__empty">
      暂无跟进记录
    </div>
    <el-timeline v-else>
      <el-timeline-item
        v-for="item in followUps"
        :key="item.id"
        :timestamp="item.createdAt"
        placement="top"
      >
        <div class="timeline-item-card">
          <div class="timeline-item-card__header">
            <el-tag size="small" :type="followTypeTag(item.followType)">
              {{ item.followType }}
            </el-tag>
            <span class="timeline-item-card__operator">{{ item.operatorDisplayName }}</span>
          </div>
          <div class="timeline-item-card__body">
            <div class="timeline-item-row">
              <span class="timeline-item-row__label">沟通内容</span>
              <span class="timeline-item-row__value">{{ item.communicatedContent }}</span>
            </div>
            <div class="timeline-item-row" v-if="item.customerFeedback">
              <span class="timeline-item-row__label">客户反馈</span>
              <span class="timeline-item-row__value">{{ item.customerFeedback }}</span>
            </div>
            <div class="timeline-item-row" v-if="item.currentConcern">
              <span class="timeline-item-row__label">当前顾虑</span>
              <span class="timeline-item-row__value">{{ item.currentConcern }}</span>
            </div>
            <div class="timeline-item-row" v-if="item.nextAction">
              <span class="timeline-item-row__label">下一步</span>
              <span class="timeline-item-row__value">{{ item.nextAction }}</span>
            </div>
            <div class="timeline-item-row" v-if="item.nextFollowAt">
              <span class="timeline-item-row__label">计划再跟</span>
              <span class="timeline-item-row__value">{{ item.nextFollowAt }}</span>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<style scoped>
.followup-timeline {
  background: #fafcff;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid rgba(37, 99, 235, 0.06);
}

.followup-timeline__empty {
  text-align: center;
  color: #94a3b8;
  padding: 32px 0;
}

.timeline-item-card {
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(37, 99, 235, 0.04);
}

.timeline-item-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.timeline-item-card__operator {
  color: #64748b;
  font-size: 13px;
}

.timeline-item-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.timeline-item-row {
  display: flex;
  gap: 10px;
  font-size: 13px;
  line-height: 1.6;
}

.timeline-item-row__label {
  color: #94a3b8;
  min-width: 72px;
  flex-shrink: 0;
}

.timeline-item-row__value {
  color: #172033;
}
</style>
