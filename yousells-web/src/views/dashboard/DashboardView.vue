<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { RouteName } from "@/router/route-names";
import { useAuthStore } from "@/stores/auth";
import { useUserStore } from "@/stores/user";
import PageSection from "@/components/app/PageSection.vue";
import DashboardStatCards from "@/components/dashboard/DashboardStatCards.vue";
import DashboardTaskList from "@/components/dashboard/DashboardTaskList.vue";
import DashboardCustomerList from "@/components/dashboard/DashboardCustomerList.vue";
import { fetchDashboardOverview } from "@/api/dashboard";
import type { DashboardOverview } from "@/types/dashboard";
import { isUnauthorizedError } from "@/utils/request-error";
import {
  User,
  DocumentChecked,
  List,
  ChatLineSquare,
  TrendCharts,
  PieChart,
  Bell,
  Calendar,
  Warning
} from "@element-plus/icons-vue";

const router = useRouter();
const authStore = useAuthStore();
const userStore = useUserStore();

const loading = ref(false);
const error = ref(false);
const overview = ref<DashboardOverview | null>(null);
const chartLoading = ref(true);

// Charts refs
const trendChartRef = ref<HTMLDivElement | null>(null);
const pieChartRef = ref<HTMLDivElement | null>(null);
let trendChartInstance: any = null;
let pieChartInstance: any = null;



const greeting = computed(() => {
  const hour = new Date().getHours();
  const name = authStore.currentUser?.realName ?? "";
  if (hour >= 5 && hour < 12) return `早安，${name} ☀️`;
  if (hour >= 12 && hour < 18) return `下午好，${name} 🌤️`;
  return `晚上好，${name} 🌙`;
});

const todaySummary = computed(() => {
  if (!overview.value) return "数据加载中...";
  const pending = overview.value.todayPendingFollowCount ?? 0;
  const overdue = overview.value.overdueCustomerCount ?? 0;
  const tasks = overview.value.todayTasks?.length ?? 0;
  const parts: string[] = [];
  if (pending > 0) parts.push(`今日 ${pending} 项待跟进`);
  if (overdue > 0) parts.push(`${overdue} 位客户已逾期`);
  if (tasks > 0) parts.push(`${tasks} 个任务待处理`);
  if (parts.length === 0) return "今日暂无待办，保持良好的工作状态 💪";
  return parts.join("，") + "，加油！";
});

const stats = computed(() => {
  if (!overview.value) {
    return [
      { label: "客户总数", value: 0, icon: "Users" },
      { label: "今日待跟进", value: 0, icon: "Bell" },
      { label: "逾期客户", value: 0, icon: "Warning" },
      { label: "本周新增", value: 0, icon: "Plus" },
      { label: "高意向客户", value: 0, icon: "Star" },
      { label: "本月成交", value: 0, icon: "Success" }
    ];
  }
  return [
    { label: "客户总数", value: overview.value.totalCustomerCount ?? 0, icon: "Users" },
    { label: "今日待跟进", value: overview.value.todayPendingFollowCount ?? 0, icon: "Bell" },
    { label: "逾期客户", value: overview.value.overdueCustomerCount ?? 0, icon: "Warning" },
    { label: "本周新增", value: overview.value.recentNewCustomerCount ?? 0, icon: "Plus" },
    { label: "高意向客户", value: overview.value.highIntentCustomerCount ?? 0, icon: "Star" },
    { label: "本月成交", value: overview.value.monthlyClosedCount ?? 0, icon: "Success" }
  ];
});

const quickActions = [
  { label: "新建客户", icon: User, color: "#2563eb", route: RouteName.CustomerList, action: () => router.push({ name: RouteName.CustomerList }) },
  { label: "写日报", icon: DocumentChecked, color: "#10b981", route: RouteName.DailyReport, action: () => router.push({ name: RouteName.DailyReport }) },
  { label: "新建任务", icon: List, color: "#f59e0b", route: RouteName.TaskBoard, action: () => router.push({ name: RouteName.TaskBoard }) },
  { label: "提问题", icon: ChatLineSquare, color: "#8b5cf6", route: RouteName.TopicList, action: () => router.push({ name: RouteName.TopicList }) }
];

async function loadOverview() {
  error.value = false;
  loading.value = true;
  try {
    overview.value = await fetchDashboardOverview();
    // Update charts with real data after overview loads
    await updateCharts();
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

async function updateCharts() {
  if (!overview.value) return;
  chartLoading.value = true;
  try {
    const echarts = await import("echarts");
    await nextTick();

    // Update trend chart
    if (trendChartRef.value && overview.value.trendData?.length > 0) {
      if (!trendChartInstance) {
        trendChartInstance = echarts.init(trendChartRef.value);
      }
      const isDark = document.documentElement.getAttribute("data-theme") === "dark";
      const lineColor = isDark ? "#60a5fa" : "#2563eb";
      const gridColor = isDark ? "#334155" : "#f1f5f9";
      const textColor = isDark ? "#94a3b8" : "#64748b";
      trendChartInstance.setOption({
        tooltip: {
          trigger: "axis",
          backgroundColor: isDark ? "#1e293b" : "#fff",
          borderColor: isDark ? "#334155" : "#e2e8f0",
          textStyle: { color: isDark ? "#f1f5f9" : "#1e293b" }
        },
        grid: { left: "3%", right: "4%", bottom: "3%", top: "10%", containLabel: true },
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: overview.value.trendData.map(d => d.date),
          axisLine: { lineStyle: { color: gridColor } },
          axisLabel: { color: textColor, fontSize: 11 }
        },
        yAxis: {
          type: "value",
          axisLine: { show: false },
          splitLine: { lineStyle: { color: gridColor } },
          axisLabel: { color: textColor, fontSize: 11 }
        },
        series: [{
          name: "新增客户",
          type: "line",
          smooth: true,
          symbol: "circle",
          symbolSize: 6,
          showSymbol: false,
          lineStyle: { color: lineColor, width: 3 },
          itemStyle: { color: lineColor },
          areaStyle: {
            color: {
              type: "linear",
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: isDark ? "rgba(96, 165, 250, 0.25)" : "rgba(37, 99, 235, 0.2)" },
                { offset: 1, color: isDark ? "rgba(96, 165, 250, 0.01)" : "rgba(37, 99, 235, 0.01)" }
              ]
            }
          },
          data: overview.value.trendData.map(d => d.count)
        }]
      });
    }

    // Update donut chart (intent distribution)
    if (pieChartRef.value && overview.value && overview.value.intentDistribution?.length > 0) {
      if (!pieChartInstance) {
        pieChartInstance = echarts.init(pieChartRef.value);
      }
      const isDark = document.documentElement.getAttribute("data-theme") === "dark";
      const textColor = isDark ? "#94a3b8" : "#64748b";
      pieChartInstance.setOption({
        tooltip: {
          trigger: "item",
          formatter: "{b}: {c} ({d}%)",
          backgroundColor: isDark ? "#1e293b" : "#fff",
          borderColor: isDark ? "#334155" : "#e2e8f0",
          textStyle: { color: isDark ? "#f1f5f9" : "#1e293b" }
        },
        legend: {
          bottom: "0%",
          left: "center",
          itemWidth: 10,
          itemHeight: 10,
          textStyle: { color: textColor, fontSize: 11 }
        },
        series: [{
          type: "pie",
          radius: ["40%", "70%"],
          center: ["50%", "45%"],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: isDark ? "#1e293b" : "#fff",
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: "bold" }
          },
          data: overview.value.intentDistribution.map(d => ({ name: d.intent, value: d.count })),
          color: isDark
            ? ["#60a5fa", "#34d399", "#fbbf24", "#f87171"]
            : ["#2563eb", "#3b82f6", "#60a5fa", "#93c5fd"]
        }]
      }, true);
    }
  } catch {
    // echarts not available
  } finally {
    chartLoading.value = false;
  }
}

function handleResize() {
  trendChartInstance?.resize();
  pieChartInstance?.resize();
}

function handleStatClick(label: string) {
  const queryMap: Record<string, Record<string, string>> = {
    "客户总数": {},
    "高意向客户": { intent: "很稳" },
    "本月成交": { progress: "成交" }
  };
  router.push({
    name: RouteName.CustomerList,
    query: queryMap[label] ?? {}
  });
}

onMounted(() => {
  void loadOverview();
  void userStore.loadUsers();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  trendChartInstance?.dispose();
  pieChartInstance?.dispose();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="首页看板"
      description="团队客户跟进概览，一眼看清今日待办、重点客户与协作进度。"
    >
      <template #extra>
        <el-button :loading="loading" @click="loadOverview">刷新数据</el-button>
      </template>

      <!-- Welcome Banner -->
      <div class="dashboard-welcome">
        <div>
          <h2 class="dashboard-welcome__title">{{ greeting }}</h2>
          <p class="dashboard-welcome__summary">{{ todaySummary }}</p>
        </div>
        <el-icon class="dashboard-welcome__icon"><Calendar /></el-icon>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <div
          v-for="action in quickActions"
          :key="action.label"
          class="quick-action-card"
          @click="action.action"
        >
          <div class="quick-action__icon" :style="{ background: action.color + '12', color: action.color }">
            <el-icon size="20"><component :is="action.icon" /></el-icon>
          </div>
          <span class="quick-action__label">{{ action.label }}</span>
        </div>
      </div>

      <!-- Stat Cards -->
      <DashboardStatCards :stats :loading @click="handleStatClick" />

      <!-- Charts -->
      <div class="dashboard-charts">
        <div class="chart-card">
          <div class="chart-card__header">
            <el-icon><TrendCharts /></el-icon>
            <span>近30天客户增长趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-card__body" v-loading="chartLoading" />
        </div>
        <div class="chart-card">
          <div class="chart-card__header">
            <el-icon><PieChart /></el-icon>
            <span>客户意向分布</span>
          </div>
          <div ref="pieChartRef" class="chart-card__body" v-loading="chartLoading" />
        </div>
      </div>

      <!-- Error State -->
      <div v-if="error" class="dashboard-error">
        <p>数据加载失败，请点击刷新重试</p>
      </div>

      <!-- Todo Lists -->
      <div v-else-if="overview" class="dashboard-todos">
        <div class="todo-card">
          <div class="todo-card__header">
            <el-icon><Bell /></el-icon>
            <span>今日公共安排</span>
          </div>
          <DashboardTaskList :tasks="overview.todayTasks" :loading />
        </div>
        <div class="todo-card">
          <div class="todo-card__header">
            <el-icon><User /></el-icon>
            <span>重点客户</span>
          </div>
          <DashboardCustomerList :customers="overview.focusCustomers" :loading />
        </div>
        <div class="todo-card" :class="{ 'todo-card--warning': (overview.silentCustomers?.length ?? 0) > 5, 'todo-card--danger': (overview.silentCustomers?.length ?? 0) > 10 }">
          <div class="todo-card__header">
            <div class="todo-card__header-title">
              <el-icon><Warning /></el-icon>
              <span>沉默客户</span>
              <el-tag v-if="overview.silentCustomers?.length" :type="(overview.silentCustomers.length > 10) ? 'danger' : (overview.silentCustomers.length > 5) ? 'warning' : 'info'" size="small">
                {{ overview.silentCustomers.length }} 位
              </el-tag>
            </div>
          </div>
          <DashboardCustomerList :customers="overview.silentCustomers" :loading />
        </div>
      </div>
    </PageSection>
  </div>
</template>

<style scoped>
.dashboard-welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-radius: var(--radius-lg);
  color: #fff;
}

.dashboard-welcome__title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
}

.dashboard-welcome__summary {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.dashboard-welcome__icon {
  font-size: 32px;
  color: rgba(255, 255, 255, 0.3);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}

.quick-action-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-1px);
}

.quick-action__icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-md);
}

.quick-action__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.dashboard-charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  box-shadow: var(--shadow-card);
}

.chart-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.chart-card__body {
  height: 220px;
  width: 100%;
}

.dashboard-todos {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.todo-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  box-shadow: var(--shadow-card);
}

.todo-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.todo-card__header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.todo-card--warning {
  border-color: var(--color-warning);
  box-shadow: 0 0 0 1px var(--color-warning-soft), var(--shadow-card);
}

.todo-card--danger {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 1px var(--color-danger-soft), var(--shadow-card);
}

@media (max-width: 1100px) {
  .quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .dashboard-charts {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .quick-actions {
    grid-template-columns: 1fr 1fr;
  }
  .dashboard-charts {
    grid-template-columns: 1fr;
  }
  .dashboard-todos {
    grid-template-columns: 1fr;
  }
  .dashboard-welcome {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
