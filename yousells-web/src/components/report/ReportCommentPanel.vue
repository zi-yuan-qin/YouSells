<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { ReportComment } from "@/types/report";
import { fetchReportComments, createReportComment } from "@/api/report";

const props = defineProps<{
  reportType: string;
  reportId: number;
}>();

const emit = defineEmits<{
  (e: "commentAdded"): void;
}>();

const comments = ref<ReportComment[]>([]);
const loading = ref(false);
const commentText = ref("");
const page = ref(1);
const total = ref(0);

async function loadComments() {
  loading.value = true;
  try {
    const res = await fetchReportComments(props.reportType, props.reportId, page.value, 20);
    comments.value = res.list;
    total.value = res.total;
  } catch (e) {
    ElMessage.error("加载评论失败");
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (!commentText.value.trim()) return;
  try {
    await createReportComment(props.reportType, props.reportId, commentText.value.trim());
    ElMessage.success("评论成功");
    commentText.value = "";
    emit("commentAdded");
    await loadComments();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "评论失败");
  }
}

function formatTime(dateStr: string) {
  return new Date(dateStr).toLocaleString("zh-CN", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}

onMounted(loadComments);
</script>

<template>
  <div class="comment-panel">
    <div class="comment-input">
      <el-input
        v-model="commentText"
        type="textarea"
        :rows="2"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <el-button type="primary" size="small" @click="handleSubmit">发送</el-button>
    </div>

    <div v-loading="loading" class="comment-list">
      <div v-if="comments.length === 0" class="comment-empty">暂无评论，来抢沙发吧</div>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-item__avatar">{{ c.userRealName.charAt(0) }}</div>
        <div class="comment-item__body">
          <div class="comment-item__meta">
            <span class="comment-item__name">{{ c.userRealName }}</span>
            <span class="comment-item__time">{{ formatTime(c.createdAt) }}</span>
          </div>
          <div class="comment-item__content">{{ c.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-panel {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}
.comment-input {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.comment-input .el-textarea {
  flex: 1;
}
.comment-list {
  max-height: 300px;
  overflow-y: auto;
}
.comment-empty {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 16px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-item__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.comment-item__body {
  flex: 1;
}
.comment-item__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.comment-item__name {
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-primary);
}
.comment-item__time {
  font-size: 12px;
  color: var(--color-text-muted);
}
.comment-item__content {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
