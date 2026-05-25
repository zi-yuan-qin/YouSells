<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import WeeklyReportForm from "@/components/report/WeeklyReportForm.vue";
import ReportHistoryPanel from "@/components/report/ReportHistoryPanel.vue";
import { fetchWeeklyReport, fetchWeeklyReportHistory } from "@/api/report";
import type { WeeklyReport } from "@/types/report";

const loading = ref(false);
const report = ref<WeeklyReport | null>(null);
const history = ref<WeeklyReport[]>([]);
const historyTotal = ref(0);
const historyPage = ref(1);
const historyLoading = ref(false);

function currentWeekKey(): string {
  const now = new Date();
  const year = now.getFullYear();
  const start = new Date(year, 0, 1);
  const days = Math.floor((now.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));
  const week = Math.ceil((days + start.getDay() + 1) / 7);
  return `${year}-W${String(week).padStart(2, "0")}`;
}

const thisWeek = computed(() => currentWeekKey());
const submitted = computed(() => report.value !== null && report.value.id !== null);

async function loadThisWeekReport() {
  loading.value = true;
  try {
    report.value = await fetchWeeklyReport(thisWeek.value);
  } catch (e) {
    report.value = null;
    ElMessage.error(e instanceof Error ? e.message : "周报加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadHistory(p = 1, ps = 20) {
  historyLoading.value = true;
  try {
    const data = await fetchWeeklyReportHistory(p, ps);
    history.value = data.list;
    historyTotal.value = data.total;
    historyPage.value = p;
  } catch {
    // non-critical
  } finally {
    historyLoading.value = false;
  }
}

function onSubmitted() {
  void loadThisWeekReport();
  void loadHistory(1);
}

onMounted(() => {
  void loadThisWeekReport();
  void loadHistory();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="周报"
      description="系统自动汇总本周数据，只需填写总结/复盘/下周计划"
    >
      <div class="report-page-grid">
        <div class="report-page-grid__form">
          <div v-if="loading" v-loading="true" style="min-height:200px" />
          <div v-else-if="submitted" class="report-form-card">
            <h3 class="report-form-card__title">本周周报</h3>
            <el-alert type="info" :closable="false" style="margin-bottom:16px">
              此周报已提交，当周内可修改
            </el-alert>
            <div class="readonly-field">
              <span class="readonly-field__label">本周总结</span>
              <p>{{ report?.summary }}</p>
            </div>
            <div class="readonly-field" v-if="report?.issues">
              <span class="readonly-field__label">问题复盘</span>
              <p>{{ report?.issues }}</p>
            </div>
            <div class="readonly-field">
              <span class="readonly-field__label">下周计划</span>
              <p>{{ report?.nextWeekPlan }}</p>
            </div>
            <div class="auto-stats">
              <el-tag size="small" type="info">新增客户 {{ report?.newCustomerCount ?? 0 }}</el-tag>
              <el-tag size="small" type="info">跟进 {{ report?.followUpCount ?? 0 }} 次</el-tag>
              <el-tag size="small" type="info">推进 {{ report?.progressAdvanceCount ?? 0 }}</el-tag>
              <el-tag size="small" type="info">课程达成 {{ report?.convertedCount ?? 0 }}</el-tag>
              <el-tag size="small" type="info">完成任务 {{ report?.taskCompletedCount ?? 0 }}</el-tag>
            </div>
          </div>
          <WeeklyReportForm v-else :week-key="thisWeek" @submitted="onSubmitted" />
        </div>
        <div class="report-page-grid__history">
          <ReportHistoryPanel
            :reports="history"
            :total="historyTotal"
            :page="historyPage"
            :page-size="20"
            report-type="weekly"
            :loading="historyLoading"
            @page-change="loadHistory"
          />
        </div>
      </div>
    </PageSection>
  </div>
</template>

<style scoped>
.report-page-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
@media (max-width: 900px) {
  .report-page-grid { grid-template-columns: 1fr; }
}
.report-form-card {
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}
.report-form-card__title {
  margin: 0 0 16px;
  font-size: 18px;
}
.readonly-field {
  margin-bottom: 14px;
}
.readonly-field__label {
  font-size: 12px;
  color: var(--color-text-muted);
  display: block;
  margin-bottom: 4px;
}
.readonly-field p {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.auto-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border);
}
</style>
