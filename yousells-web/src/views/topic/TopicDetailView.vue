<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import TopicReplyList from "@/components/topic/TopicReplyList.vue";
import TopicReplyForm from "@/components/topic/TopicReplyForm.vue";
import { fetchTopicDetail, createReply, markSolved, markSolution } from "@/api/topic";
import { useAuthStore } from "@/stores/auth";
import type { TopicDetail } from "@/types/topic";
import { formatDate } from "@/utils/format";
import { RouteName } from "@/router/route-names";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const topic = ref<TopicDetail | null>(null);
const loading = ref(false);
const error = ref(false);
const notFound = ref(false);
const replyLoading = ref(false);
const markLoading = ref(false);

const isAuthor = computed(() => {
  if (!topic.value || !authStore.currentUser) return false;
  return authStore.currentUser.userId === topic.value.authorUserId;
});

async function loadDetail() {
  error.value = false;
  notFound.value = false;
  loading.value = true;
  try {
    const id = String(route.params.id);
    if (!/^\d+$/.test(id)) {
      notFound.value = true;
      return;
    }
    const data = await fetchTopicDetail(id);
    topic.value = data;
  } catch (e: any) {
    if (e?.response?.status === 404) {
      notFound.value = true;
    } else {
      error.value = true;
    }
  } finally {
    loading.value = false;
  }
}

async function handleMarkSolved() {
  if (!topic.value) return;
  markLoading.value = true;
  try {
    await markSolved(topic.value.id);
    ElMessage.success("已标记为已解决");
    await loadDetail();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
  } finally {
    markLoading.value = false;
  }
}

async function handleMarkSolution(replyId: number) {
  if (!topic.value) return;
  // 乐观更新
  topic.value.replies.forEach((r) => {
    r.isSolution = r.id === replyId;
  });
  markLoading.value = true;
  try {
    await markSolution(topic.value.id, replyId);
    ElMessage.success("已标记为最佳方案");
    await loadDetail();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
    await loadDetail(); // 回滚兜底
  } finally {
    markLoading.value = false;
  }
}

async function handleSubmitReply(data: { content: string }) {
  if (!topic.value) return;
  replyLoading.value = true;
  try {
    await createReply(topic.value.id, data);
    ElMessage.success("方案提交成功");
    await loadDetail();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "提交失败");
  } finally {
    replyLoading.value = false;
  }
}

function goBack() {
  router.push({ name: RouteName.TopicList });
}

onMounted(() => {
  void loadDetail();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      :title="topic?.title || '问题详情'"
      description=""
    >
      <template #extra>
        <el-button @click="goBack">返回列表</el-button>
      </template>

      <div v-if="loading" v-loading="true" style="min-height: 200px" />

      <div v-else-if="notFound" class="detail-error">
        <EmptyState title="内容不存在" description="该问题可能已被删除或链接有误" />
        <el-button type="primary" @click="goBack">返回攻略区</el-button>
      </div>

      <div v-else-if="error" class="detail-error">
        <p>加载失败</p>
        <el-button size="small" @click="loadDetail">重试</el-button>
      </div>

      <div v-else-if="topic" class="topic-detail">
        <!-- 问题信息 -->
        <div class="topic-header">
          <div class="topic-meta">
            <el-tag size="small">{{ topic.category }}</el-tag>
            <span class="meta-text">{{ topic.authorName }} · {{ formatDate(topic.createdAt) }}</span>
            <el-tag v-if="topic.solved" type="success" size="small">已解决</el-tag>
          </div>
          <p v-if="topic.description" class="topic-description">{{ topic.description }}</p>
        </div>

        <!-- 标记已解决 -->
        <div v-if="isAuthor && !topic.solved" class="topic-actions">
          <el-button
            type="success"
            size="small"
            :loading="markLoading"
            @click="handleMarkSolved"
          >
            标记为已解决
          </el-button>
        </div>

        <!-- 回答列表 -->
        <div class="reply-section">
          <h3 class="section-title">
            回答
            <span v-if="(topic.replies?.length ?? 0) > 0" class="reply-count">({{ topic.replies.length }})</span>
          </h3>
          <TopicReplyList
            :replies="topic.replies"
            :solved="topic.solved"
            :is-author="isAuthor"
            :loading="loading"
            @mark-solution="handleMarkSolution"
          />
        </div>

        <!-- 写回答 -->
        <div v-if="!topic.solved" class="reply-form-section">
          <TopicReplyForm
            :loading="replyLoading"
            @submit="handleSubmitReply"
          />
        </div>
      </div>
    </PageSection>
  </div>
</template>

<style scoped>
.topic-detail {
  max-width: 800px;
}

.topic-header {
  margin-bottom: 20px;
}

.topic-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.meta-text {
  font-size: 13px;
  color: var(--color-text-muted);
}

.topic-description {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

.topic-actions {
  margin-bottom: 20px;
}

.reply-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.reply-count {
  font-weight: 400;
  color: var(--color-text-muted);
  font-size: 14px;
}

.reply-form-section {
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}

.detail-error {
  text-align: center;
  padding: 40px 0;
}

.detail-error p {
  color: var(--color-text-muted);
  margin-bottom: 12px;
}
</style>
