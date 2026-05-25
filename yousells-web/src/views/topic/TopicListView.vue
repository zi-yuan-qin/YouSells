<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import TopicFilterBar from "@/components/topic/TopicFilterBar.vue";
import TopicCard from "@/components/topic/TopicCard.vue";
import TopicCreateDialog from "@/components/topic/TopicCreateDialog.vue";
import { fetchTopics, createTopic } from "@/api/topic";
import type { TopicListItem, TopicCreateRequest } from "@/types/topic";
import { RouteName } from "@/router/route-names";

const router = useRouter();

const loading = ref(false);
const error = ref(false);
const topics = ref<TopicListItem[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  pageSize: 10,
  category: "",
  keyword: ""
});

const createDialogVisible = ref(false);
const createLoading = ref(false);

async function loadTopics() {
  error.value = false;
  loading.value = true;
  try {
    const data = await fetchTopics(query);
    topics.value = data.list;
    total.value = data.total;
  } catch (e) {
    error.value = true;
    ElMessage.error(e instanceof Error ? e.message : "问题列表加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  query.page = 1;
  void loadTopics();
}

function onPageChange(page: number) {
  query.page = page;
  void loadTopics();
}

function goDetail(topicId: number) {
  router.push({ name: RouteName.TopicDetail, params: { id: topicId } });
}

async function onCreateSubmit(data: TopicCreateRequest) {
  createLoading.value = true;
  try {
    await createTopic(data);
    ElMessage.success("提问成功");
    createDialogVisible.value = false;
    query.page = 1;
    await loadTopics();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "提问失败");
  } finally {
    createLoading.value = false;
  }
}

onMounted(() => {
  void loadTopics();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="攻略区"
      description="团队内部协作知识库，全员提问、全员出方案"
    >
      <template #extra>
        <el-button type="primary" @click="createDialogVisible = true">
          + 提问
        </el-button>
      </template>

      <TopicFilterBar
        v-model="query"
        :loading
        @search="onSearch"
      />

      <div v-if="error" class="dashboard-error">
        <p>数据加载失败，请点击刷新重试</p>
        <el-button size="small" @click="loadTopics">刷新</el-button>
      </div>

      <div v-else-if="topics.length === 0 && !loading">
        <EmptyState title="暂无问题" description="攻略区空空如也，来发起第一个讨论吧" action-text="发起提问" @action="createDialogVisible = true" />
      </div>

      <div v-else class="topic-list">
        <TopicCard
          v-for="topic in topics"
          :key="topic.id"
          :topic
          @click="goDetail"
        />
      </div>

      <el-pagination
        v-if="total > 0"
        :current-page="query.page"
        :page-size="query.pageSize"
        :total
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="onPageChange"
      />
    </PageSection>

    <TopicCreateDialog
      v-model:visible="createDialogVisible"
      :loading="createLoading"
      @submit="onCreateSubmit"
    />
  </div>
</template>

<style scoped>
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dashboard-error {
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-muted);
}

.dashboard-error p {
  margin-bottom: 12px;
}
</style>
