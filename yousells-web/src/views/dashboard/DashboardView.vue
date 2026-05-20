<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import DashboardStatCards from "@/components/dashboard/DashboardStatCards.vue";
import DashboardTaskList from "@/components/dashboard/DashboardTaskList.vue";
import DashboardCustomerList from "@/components/dashboard/DashboardCustomerList.vue";
import { fetchDashboardOverview } from "@/api/dashboard";
import type { DashboardOverview } from "@/types/dashboard";
import { isUnauthorizedError } from "@/utils/request-error";

const loading = ref(false);
const error = ref(false);
const overview = ref<DashboardOverview | null>(null);

const stats = computed(() => {
  if (!overview.value) {
    return [
      { label: "今日待跟进", value: 0 },
      { label: "逾期客户", value: 0 },
      { label: "最近新增", value: 0 },
      { label: "高意向客户", value: 0 }
    ];
  }
  return [
    { label: "今日待跟进", value: overview.value.todayPendingFollowCount },
    { label: "逾期客户", value: overview.value.overdueCustomerCount },
    { label: "最近新增", value: overview.value.recentNewCustomerCount },
    { label: "高意向客户", value: overview.value.highIntentCustomerCount }
  ];
});

async function loadOverview() {
  error.value = false;
  loading.value = true;
  try {
    overview.value = await fetchDashboardOverview();
  } catch (e) {
    if (isUnauthorizedError(e)) {
      return;
    }
    error.value = true;
    ElMessage.error(e instanceof Error ? e.message : "首页看板加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadOverview();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="首页看板"
      description="这里已经接上了首页概览接口，后续嘉诚可以直接在这个壳子里继续补仪表卡片、预警列表和更多联动数据。"
    >
      <template #extra>
        <el-button :loading="loading" @click="loadOverview">刷新数据</el-button>
      </template>

      <DashboardStatCards :stats :loading />

      <div v-if="error" class="dashboard-error">
        <p>数据加载失败，请点击刷新重试</p>
      </div>

      <div v-else-if="overview" class="split-grid">
        <DashboardTaskList :tasks="overview.todayTasks" :loading />
        <DashboardCustomerList :customers="overview.focusCustomers" :loading />
      </div>
    </PageSection>
  </div>
</template>
