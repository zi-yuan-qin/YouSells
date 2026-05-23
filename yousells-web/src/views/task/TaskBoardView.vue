<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import EmptyState from "@/components/ui/EmptyState.vue";
import TaskBoardToolbar from "@/components/task/TaskBoardToolbar.vue";
import TaskBoardColumn from "@/components/task/TaskBoardColumn.vue";
import TaskEditDialog from "@/components/task/TaskEditDialog.vue";
import { fetchTaskBoard, updateTask } from "@/api/task";
import type { TaskBoardColumn as TaskBoardColumnType, TaskBoardItem } from "@/types/task";

const loading = ref(false);
const columns = ref<TaskBoardColumnType[]>([]);
const dialogVisible = ref(false);
const editingTask = ref<TaskBoardItem | null>(null);

async function loadBoard() {
  loading.value = true;
  try {
    columns.value = await fetchTaskBoard();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "公共安排看板加载失败");
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  editingTask.value = null;
  dialogVisible.value = true;
}

function openEditDialog(task: TaskBoardItem) {
  editingTask.value = task;
  dialogVisible.value = true;
}

function onDialogClose() {
  dialogVisible.value = false;
  editingTask.value = null;
}

async function onTaskSaved() {
  await loadBoard();
}

async function handleStatusChange(task: TaskBoardItem, newStatus: string) {
  try {
    await updateTask(task.id, {
      taskTitle: task.taskTitle,
      status: newStatus,
      priority: task.priority,
      ownerUserId: task.ownerUserId,
      dueAt: task.dueAt
    });
    ElMessage.success("状态已更新");
    await loadBoard();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "更新失败");
  }
}

onMounted(() => {
  void loadBoard();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="公共安排区"
      description="任务看板，按待开始 / 进行中 / 已完成分列展示"
    >
      <TaskBoardToolbar
        :loading="loading"
        @refresh="loadBoard"
        @create-task="openCreateDialog"
      />

      <div class="board-grid">
        <TaskBoardColumn
          v-for="column in columns"
          :key="column.status"
          :column="column"
          :loading="loading"
          @task-click="openEditDialog"
          @task-status-change="handleStatusChange"
        />
      </div>

      <EmptyState v-if="!loading && columns.length === 0" title="暂无任务" description="看板空空如也，点击上方按钮创建第一个任务" />
    </PageSection>

    <TaskEditDialog
      :visible="dialogVisible"
      :task="editingTask"
      @close="onDialogClose"
      @saved="onTaskSaved"
    />
  </div>
</template>

<style scoped>
.board-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 900px) {
  .board-grid {
    grid-template-columns: 1fr;
  }
}
</style>
