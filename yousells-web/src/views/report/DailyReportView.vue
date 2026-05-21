<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import DailyReportForm from "@/components/report/DailyReportForm.vue";
import ReportHistoryPanel from "@/components/report/ReportHistoryPanel.vue";
import { fetchDailyReports } from "@/api/report";
import type { DailyReport } from "@/types/report";

const loading = ref(false);
const reports = ref<DailyReport[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);

async function loadReports(p = 1, ps = 20) {
  loading.value = true;
  try {
    const data = await fetchDailyReports(p, ps);
    reports.value = data.list;
    total.value = data.total;
    page.value = p;
    pageSize.value = ps;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "日报加载失败");
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
      title="日报"
      description="提交每日工作报告，查看历史记录"
    >
      <div class="report-page-grid">
        <div class="report-page-grid__form">
          <DailyReportForm @submitted="onSubmitted" />
        </div>
        <div class="report-page-grid__history">
          <ReportHistoryPanel
            :reports="reports"
            :total="total"
            :page="page"
            :page-size="pageSize"
            report-type="daily"
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
