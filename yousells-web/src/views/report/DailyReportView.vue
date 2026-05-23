<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import DailyReportForm from "@/components/report/DailyReportForm.vue";
import ReportHistoryPanel from "@/components/report/ReportHistoryPanel.vue";
import { fetchDailyReport, fetchDailyReportHistory } from "@/api/report";
import type { DailyReport } from "@/types/report";

const loading = ref(false);
const report = ref<DailyReport | null>(null);
const history = ref<DailyReport[]>([]);
const historyTotal = ref(0);
const historyPage = ref(1);
const historyLoading = ref(false);

const today = computed(() => new Date().toISOString().slice(0, 10));
const submitted = computed(() => report.value !== null && report.value.id !== null);

async function loadTodayReport() {
  loading.value = true;
  try {
    report.value = await fetchDailyReport(today.value);
  } catch (e) {
    report.value = null;
    ElMessage.error(e instanceof Error ? e.message : "日报加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadHistory(p = 1) {
  historyLoading.value = true;
  try {
    const data = await fetchDailyReportHistory(p, 20);
    history.value = data.list;
    historyTotal.value = data.total;
    historyPage.value = p;
  } catch {
    // history load failure is non-critical
  } finally {
    historyLoading.value = false;
  }
}

function onSubmitted() {
  void loadTodayReport();
  void loadHistory(1);
}

onMounted(() => {
  void loadTodayReport();
  void loadHistory();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="日报"
      description="系统自动汇总今日数据，只需填写小结/问题/明天计划"
    >
      <div class="report-page-grid">
        <div class="report-page-grid__form">
          <div v-if="loading" v-loading="true" style="min-height:200px" />
          <div v-else-if="submitted" class="report-form-card">
            <h3 class="report-form-card__title">今日日报</h3>
            <el-alert type="info" :closable="false" style="margin-bottom:16px">
              此日报已提交，当天内可修改
            </el-alert>
            <div class="readonly-field">
              <span class="readonly-field__label">今日小结</span>
              <p>{{ report?.summary }}</p>
            </div>
            <div class="readonly-field" v-if="report?.issues">
              <span class="readonly-field__label">遇到的问题</span>
              <p>{{ report?.issues }}</p>
            </div>
            <div class="readonly-field">
              <span class="readonly-field__label">明天计划</span>
              <p>{{ report?.tomorrowPlan }}</p>
            </div>
            <div class="auto-stats">
              <el-tag size="small" type="info">新增客户 {{ report?.newCustomerCount ?? 0 }}</el-tag>
              <el-tag size="small" type="info">跟进 {{ report?.followUpCount ?? 0 }} 次</el-tag>
              <el-tag size="small" type="info">推进 {{ report?.progressAdvanceCount ?? 0 }}</el-tag>
              <el-tag size="small" type="info">完成任务 {{ report?.taskCompletedCount ?? 0 }}</el-tag>
            </div>
          </div>
          <DailyReportForm v-else @submitted="onSubmitted" />
        </div>
        <div class="report-page-grid__history">
          <ReportHistoryPanel
            :reports="history"
            :total="historyTotal"
            :page="historyPage"
            :page-size="20"
            report-type="daily"
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
