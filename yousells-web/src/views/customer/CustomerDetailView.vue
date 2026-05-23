<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import { stageLabel, followTypeLabel } from "@/constants/stage";
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

      <div v-if="detail" class="customer-detail-grid">
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

      <EmptyState v-else-if="!loading" title="客户不存在" description="该客户可能已被删除或 ID 有误" />

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
