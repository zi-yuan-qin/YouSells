<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import { friendlyDate, datetime } from "@/utils/format";
import CustomerProfileCard from "@/components/customer-detail/CustomerProfileCard.vue";
import CustomerMetaPanel from "@/components/customer-detail/CustomerMetaPanel.vue";
import CustomerNextActionCard from "@/components/customer-detail/CustomerNextActionCard.vue";
import FollowUpTimeline from "@/components/followup/FollowUpTimeline.vue";
import FollowUpCreateForm from "@/components/followup/FollowUpCreateForm.vue";
import { fetchCustomerDetail } from "@/api/customer-detail";
import { fetchFollowUps } from "@/api/followup";
import type { CustomerDetail } from "@/types/customer-detail";
import type { FollowUpRecord } from "@/types/followup";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<CustomerDetail | null>(null);
const followUps = ref<FollowUpRecord[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const id = String(route.params.id);
    if (!id || id === "undefined") {
      ElMessage.error("客户 ID 无效");
      return;
    }
    const [detailData, followUpData] = await Promise.all([
      fetchCustomerDetail(id),
      fetchFollowUps(id)
    ]);
    detail.value = detailData;
    followUps.value = followUpData.list;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "客户详情加载失败");
  } finally {
    loading.value = false;
  }
}

async function onDetailUpdated() {
  await loadData();
}

async function onFollowUpCreated() {
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
          @click="router.push({ path: '/customers' })"
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
                :percentage="detail.progress === '课程' ? 75 : detail.progress === '技术栈' ? 50 : 25"
                :color="detail.progress === '课程' ? '#10b981' : detail.progress === '技术栈' ? '#f59e0b' : '#3b82f6'"
                :stroke-width="8"
                :show-text="false"
                style="width: 120px"
              />
              <span class="status-bar__value">{{ detail.progress }}</span>
            </div>
          </div>
          <div class="status-bar__actions">
            <el-button
              type="primary"
              size="small"
              @click="router.push({ name: 'customer-detail', params: { id: detail.id }, query: { tab: 'followup' } })"
            >
              写跟进
            </el-button>
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
              @tags-updated="onDetailUpdated"
            />

            <CustomerNextActionCard
              :follow-ups="followUps"
              :loading="loading"
              @updated="onDetailUpdated"
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
