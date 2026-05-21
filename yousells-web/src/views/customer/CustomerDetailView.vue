<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
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
const loading = ref(false);
const detail = ref<CustomerDetail | null>(null);
const followUps = ref<FollowUpRecord[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const id = String(route.params.id ?? "1001");
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
            :detail="detail"
            :loading="loading"
            @updated="onDetailUpdated"
          />
        </div>
      </div>

      <el-empty v-else-if="!loading" description="未找到客户信息" />

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
