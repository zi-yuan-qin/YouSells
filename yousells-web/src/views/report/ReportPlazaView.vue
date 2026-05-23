<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import ReportPlazaCard from "@/components/report/ReportPlazaCard.vue";
import { fetchReportPlaza } from "@/api/report";
import { fetchUserList } from "@/api/user";
import type { ReportPlazaItem } from "@/types/report";
import type { UserListItem } from "@/types/user";

const activeTab = ref<"daily" | "weekly">("daily");
const reports = ref<ReportPlazaItem[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(5);
const total = ref(0);
const filterUserId = ref<number | null>(null);
const userList = ref<UserListItem[]>([]);

async function loadUserList() {
  try {
    userList.value = await fetchUserList();
  } catch {
    // non-critical
  }
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchReportPlaza(
      activeTab.value,
      page.value,
      pageSize.value,
      filterUserId.value
    );
    reports.value = res.list;
    total.value = res.total;
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "加载失败");
  } finally {
    loading.value = false;
  }
}

watch(activeTab, () => {
  page.value = 1;
  filterUserId.value = null;
  loadData();
});

watch(filterUserId, () => {
  page.value = 1;
  loadData();
});

function handleLikeToggled(id: number, liked: boolean, delta: number) {
  const item = reports.value.find(r => r.id === id);
  if (item) {
    item.likeCount += delta;
    item.likedByMe = liked;
  }
}

function handleCommentAdded(id: number) {
  const item = reports.value.find(r => r.id === id);
  if (item) {
    item.commentCount++;
  }
}

onMounted(() => {
  void loadUserList();
  void loadData();
});
</script>

<template>
  <div class="page-shell">
    <PageSection title="报告广场" description="查看团队成员的日报和周报">
      <template #extra>
        <div style="display: flex; gap: 12px; align-items: center;">
          <el-select
            v-model="filterUserId"
            clearable
            placeholder="按成员筛选"
            style="width: 160px"
            size="small"
          >
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="user.realName"
              :value="user.userId"
            />
          </el-select>
          <el-radio-group v-model="activeTab" size="small">
            <el-radio-button value="daily">日报</el-radio-button>
            <el-radio-button value="weekly">周报</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-loading="loading" class="plaza-list">
        <ReportPlazaCard
          v-for="item in reports"
          :key="item.id"
          :item="item"
          @like-toggled="handleLikeToggled"
          @comment-added="handleCommentAdded"
        />

        <EmptyState v-if="!loading && reports.length === 0" title="暂无报告" description="还没有人提交日报或周报，来做第一个提交的人吧" />
      </div>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="pagination"
        @current-change="loadData"
      />
    </PageSection>
  </div>
</template>

<style scoped>
.plaza-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
