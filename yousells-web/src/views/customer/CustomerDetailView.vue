<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import CustomerProfileCard from "@/components/customer-detail/CustomerProfileCard.vue";
import CustomerMetaPanel from "@/components/customer-detail/CustomerMetaPanel.vue";
import CustomerNextActionCard from "@/components/customer-detail/CustomerNextActionCard.vue";
import FollowUpTimeline from "@/components/followup/FollowUpTimeline.vue";
import FollowUpCreateForm from "@/components/followup/FollowUpCreateForm.vue";
import { fetchCustomerDetail } from "@/api/customer-detail";
import { fetchFollowUps } from "@/api/followup";
import type { CustomerDetail } from "@/types/customer-detail";
import type { FollowUpRecord } from "@/types/followup";
import { RouteName } from "@/router/route-names";

const PROGRESS_CONFIG: Record<string, { percentage: number; color: string }> = {
  "职规": { percentage: 25, color: "#3b82f6" },
  "技术栈": { percentage: 50, color: "#f59e0b" },
  "课程": { percentage: 75, color: "#10b981" },
};
const DEFAULT_PROGRESS = { percentage: 25, color: "#3b82f6" };

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<CustomerDetail | null>(null);
const followUps = ref<FollowUpRecord[]>([]);
const followUpTotal = ref(0);
const followUpPage = ref(1);
const followUpPageSize = 20;
let requestId = 0;

async function loadData() {
  const id = String(route.params.id);
  if (!id || id === "undefined") {
    ElMessage.error("客户 ID 无效");
    return;
  }

  const currentRequestId = ++requestId;
  loading.value = true;

  const [detailResult, followUpResult] = await Promise.allSettled([
    fetchCustomerDetail(id),
    fetchFollowUps(id, followUpPage.value, followUpPageSize)
  ]);

  if (currentRequestId !== requestId) return;

  if (detailResult.status === "fulfilled") {
    detail.value = detailResult.value;
  } else {
    detail.value = null;
    ElMessage.error(detailResult.reason instanceof Error ? detailResult.reason.message : "客户详情加载失败");
  }

  if (followUpResult.status === "fulfilled") {
    followUps.value = followUpResult.value.list;
    followUpTotal.value = followUpResult.value.total;
  } else {
    followUps.value = [];
    ElMessage.error(followUpResult.reason instanceof Error ? followUpResult.reason.message : "跟进记录加载失败");
  }

  loading.value = false;
}

async function onDetailUpdated() {
  await loadData();
}

async function onFollowUpCreated() {
  followUpPage.value = 1;
  await loadData();
}

async function onFollowUpPageChange(page: number) {
  followUpPage.value = page;
  await loadData();
}

onMounted(() => {
  void loadData();
});

watch(() => route.params.id, (newId, oldId) => {
  if (newId !== oldId && newId) {
    void loadData();
  }
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="客户详情与跟进记录"
      description="客户详细信息、跟进记录与下一步行动计划。"
    >
      <template #extra>
        <el-button :loading="loading" @click="loadData">刷新详情</el-button>
      </template>

      <div style="margin-bottom: 8px">
        <el-button
          text
          type="primary"
          :icon="ArrowLeft"
          @click="router.push({ name: RouteName.CustomerList })"
        >
          返回客户列表
        </el-button>
      </div>

      <div v-if="detail">
        <div class="customer-status-bar">
          <div class="status-bar__info">
            <el-tag
              size="large"
              :type="detail.intent === '很稳' ? 'success' : detail.intent === '冷淡' ? 'danger' : 'warning'"
              effect="dark"
            >
              {{ detail.intent }}
            </el-tag>
            <div class="status-bar__divider"></div>
            <div class="status-bar__progress">
              <span class="status-bar__label">当前进度</span>
              <el-progress
                :percentage="PROGRESS_CONFIG[detail.progress]?.percentage ?? DEFAULT_PROGRESS.percentage"
                :color="PROGRESS_CONFIG[detail.progress]?.color ?? DEFAULT_PROGRESS.color"
                :stroke-width="8"
                :show-text="false"
                style="width: 120px"
              />
              <span class="status-bar__value">{{ detail.progress }}</span>
            </div>
          </div>
        </div>

        <div class="customer-detail-grid">
          <div class="customer-detail-grid__left">
            <CustomerProfileCard
              :detail="detail"
              :loading="loading"
              @updated="onDetailUpdated"
            />
          </div>

          <div class="customer-detail-grid__right">
            <CustomerMetaPanel
              :detail="detail"
              :loading="loading"
            />

            <CustomerNextActionCard
              :follow-ups="followUps"
              :loading="loading"
            />
          </div>
        </div>
      </div>

      <EmptyState
        v-else-if="!loading"
        title="客户不存在"
        description="该客户可能已被删除或 ID 有误"
      />

      <div v-if="detail">
        <div class="followup-section-header">
          <h3 class="page-section__title" style="font-size: 18px; margin: 0">
            跟进时间线
          </h3>
          <FollowUpCreateForm
            :customer-id="detail.id"
            @created="onFollowUpCreated"
          />
        </div>
        <FollowUpTimeline
          :follow-ups="followUps"
          :loading="loading"
        />
        <el-pagination
          v-if="followUpTotal > followUpPageSize"
          :current-page="followUpPage"
          :page-size="followUpPageSize"
          :total="followUpTotal"
          layout="total, prev, pager, next"
          style="margin-top: 16px; justify-content: flex-end"
          @current-change="onFollowUpPageChange"
        />
      </div>
    </PageSection>
  </div>
</template>

<style scoped>
.customer-detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.customer-detail-grid__left,
.customer-detail-grid__right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.customer-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-md) var(--space-lg);
  margin-bottom: var(--space-lg);
  box-shadow: var(--shadow-card);
}

.status-bar__info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.status-bar__divider {
  width: 1px;
  height: 24px;
  background: var(--color-border);
}

.status-bar__progress {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.status-bar__label {
  font: var(--font-caption);
  color: var(--color-text-muted);
}

.status-bar__value {
  font: var(--font-h3);
  color: var(--color-text-primary);
}

.followup-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .customer-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
