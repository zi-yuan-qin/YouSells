<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import WeeklyReportForm from "@/components/report/WeeklyReportForm.vue";
import ReportHistoryPanel from "@/components/report/ReportHistoryPanel.vue";
import { fetchWeeklyReports } from "@/api/report";
import type { WeeklyReport } from "@/types/report";

const loading = ref(false);
const reports = ref<WeeklyReport[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);

async function loadReports(p = 1, ps = 20) {
  loading.value = true;
  try {
    const data = await fetchWeeklyReports(p, ps);
    reports.value = data.list;
    total.value = data.total;
    page.value = p;
    pageSize.value = ps;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "周报加载失败");
  } finally {
    loading.value = false;
  }
}

function onPageChange(p: number, ps: number) {
  void loadReports(p, ps);
}

function onSubmitted() {
  void loadReports(1, pageSize.value);
}

onMounted(() => {
  void loadReports();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="周报"
      description="提交每周工作总结，查看历史记录"
    >
      <div class="report-page-grid">
        <div class="report-page-grid__form">
          <WeeklyReportForm @submitted="onSubmitted" />
        </div>
        <div class="report-page-grid__history">
          <ReportHistoryPanel
            :reports="reports"
            :total="total"
            :page="page"
            :page-size="pageSize"
            report-type="weekly"
            :loading="loading"
            @page-change="onPageChange"
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
  .report-page-grid {
    grid-template-columns: 1fr;
  }
}
</style>
