<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import TaskBoardToolbar from "@/components/task/TaskBoardToolbar.vue";
import TaskBoardColumn from "@/components/task/TaskBoardColumn.vue";
import TaskEditDialog from "@/components/task/TaskEditDialog.vue";
import { fetchTaskBoard } from "@/api/task";
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
        />
      </div>

      <el-empty v-if="!loading && columns.length === 0" description="暂无任务数据" />
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
