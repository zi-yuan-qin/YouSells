<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import { stageLabel, followTypeLabel } from "@/constants/stage";
import { friendlyDate, datetime } from "@/utils/format";
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

      <div v-if="detail" class="detail-grid">
        <div class="detail-item">
          <div class="detail-item__label">客户昵称</div>
          <div class="detail-item__value">{{ detail.nickname }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">联系方式</div>
          <div class="detail-item__value">{{ detail.contactValue }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">当前阶段</div>
          <div class="detail-item__value">{{ stageLabel(detail.currentStage) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">当前顾虑</div>
          <div class="detail-item__value">{{ detail.currentConcern || "暂无" }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">负责人 / 协助人</div>
          <div class="detail-item__value">{{ detail.ownerDisplayName }} / {{ detail.assistantDisplayName || "暂无" }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">下次跟进</div>
          <div class="detail-item__value">{{ detail.nextFollowAction || "暂无" }}｜{{ friendlyDate(detail.nextFollowAt) }}</div>
        </div>
      </div>

      <div>
        <div class="page-section__title" style="font-size: 18px;">跟进时间线</div>
        <div v-if="followUps.length === 0" class="list-card__placeholder" style="margin-top: 12px;">暂无跟进记录</div>
        <div v-for="item in followUps" :key="item.id" class="timeline-card">
          <div class="timeline-card__meta">
            {{ datetime(item.createdAt) }}｜{{ followTypeLabel(item.followType) }}｜{{ item.operatorDisplayName }}
          </div>
          <div class="timeline-card__content">
            {{ item.communicatedContent }}
            <br />
            客户反馈：{{ item.customerFeedback || "暂无" }}
            <br />
            下一步：{{ item.nextAction || "暂无" }}
          </div>
        </div>
      </div>
    </PageSection>
  </div>
</template>
