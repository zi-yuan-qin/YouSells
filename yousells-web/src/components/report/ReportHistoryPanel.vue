<script setup lang="ts">
import type { DailyReport, WeeklyReport } from "@/types/report";

defineProps<{
  reports: (DailyReport | WeeklyReport)[];
  total: number;
  page: number;
  pageSize: number;
  reportType: "daily" | "weekly";
  loading: boolean;
}>();

const emit = defineEmits<{
  "page-change": [page: number, pageSize: number];
}>();
</script>

<template>
  <div class="history-panel" v-loading="loading">
    <h3 class="history-panel__title">
      {{ reportType === "daily" ? "日报历史" : "周报历史" }}
    </h3>

    <template v-if="reports.length > 0">
      <div
        v-for="item in reports"
        :key="item.id"
        class="history-item"
      >
        <div class="history-item__header">
          <span class="history-item__label">
            {{ reportType === "daily" ? (item as DailyReport).reportDate : (item as WeeklyReport).weekKey }}
          </span>
          <span class="history-item__user">
            {{ item.userDisplayName }}
          </span>
        </div>
        <div class="history-item__body">
          <template v-if="reportType === 'daily'">
            <div class="history-item__field">
              <span class="history-item__field-label">今日工作：</span>
              {{ (item as DailyReport).todayWork }}
            </div>
            <div class="history-item__field" v-if="(item as DailyReport).issues">
              <span class="history-item__field-label">问题：</span>
              {{ (item as DailyReport).issues }}
            </div>
            <div class="history-item__field">
              <span class="history-item__field-label">明日计划：</span>
              {{ (item as DailyReport).tomorrowPlan }}
            </div>
          </template>
          <template v-else>
            <div class="history-item__field">
              <span class="history-item__field-label">本周总结：</span>
              {{ (item as WeeklyReport).weeklySummary }}
            </div>
            <div class="history-item__field" v-if="(item as WeeklyReport).issues">
              <span class="history-item__field-label">问题：</span>
              {{ (item as WeeklyReport).issues }}
            </div>
            <div class="history-item__field">
              <span class="history-item__field-label">下周计划：</span>
              {{ (item as WeeklyReport).nextWeekPlan }}
            </div>
          </template>
        </div>
      </div>

      <div class="history-panel__pagination">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next, sizes"
          small
          @update:current-page="emit('page-change', $event, pageSize)"
          @update:page-size="emit('page-change', page, $event)"
        />
      </div>
    </template>

    <div v-else class="history-panel__empty">
      暂无{{ reportType === "daily" ? "日报" : "周报" }}记录
    </div>
  </div>
</template>

<style scoped>
.history-panel {
  background: #fafcff;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid rgba(37, 99, 235, 0.06);
}

.history-panel__title {
  margin: 0 0 16px;
  font-size: 18px;
}

.history-item {
  padding: 14px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(37, 99, 235, 0.04);
}

.history-item + .history-item {
  margin-top: 10px;
}

.history-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-item__label {
  font-weight: 700;
  font-size: 14px;
  color: #2563eb;
}

.history-item__user {
  color: #64748b;
  font-size: 13px;
}

.history-item__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-item__field {
  font-size: 13px;
  line-height: 1.6;
  color: #42506a;
}

.history-item__field-label {
  color: #94a3b8;
}

.history-panel__empty {
  text-align: center;
  color: #94a3b8;
  padding: 32px 0;
}

.history-panel__pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
