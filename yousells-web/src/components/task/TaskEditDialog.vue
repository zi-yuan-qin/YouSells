<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import type { TaskBoardItem, TaskCreateRequest, TaskUpdateRequest } from "@/types/task";
import { createTask, updateTask } from "@/api/task";

const props = defineProps<{
  visible: boolean;
  task: TaskBoardItem | null;
}>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const isEdit = ref(false);
const submitting = ref(false);
const formRef = ref();

const form = reactive<TaskCreateRequest & { status?: string; taskDescription?: string | null; nextAction?: string | null }>({
  taskTitle: "",
  taskType: null,
  taskDescription: null,
  priority: "",
  ownerUserId: 1,
  assistantUserId: null,
  dueAt: null,
  status: "待开始",
  nextAction: null
});

const rules = {
  taskTitle: [{ required: true, message: "请填写任务标题", trigger: "blur" }],
  ownerUserId: [{ required: true, message: "请选择负责人", trigger: "change" }]
};

watch(
  () => [props.visible, props.task] as const,
  ([v, t]) => {
    if (v) {
      if (t) {
        isEdit.value = true;
        form.taskTitle = t.taskTitle;
        form.taskType = t.taskType;
        form.priority = t.priority;
        form.ownerUserId = 1;
        form.assistantUserId = null;
        form.dueAt = t.dueAt;
        form.status = t.status;
        form.taskDescription = null;
        form.nextAction = t.nextAction;
      } else {
        isEdit.value = false;
        form.taskTitle = "";
        form.taskType = null;
        form.priority = "";
        form.ownerUserId = 1;
        form.assistantUserId = null;
        form.dueAt = null;
        form.status = "待开始";
        form.taskDescription = null;
        form.nextAction = null;
      }
    }
  }
);

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (isEdit.value && props.task) {
      await updateTask(props.task.id, {
        taskTitle: form.taskTitle,
        status: form.status || "待开始",
        taskDescription: form.taskDescription,
        priority: form.priority || undefined,
        ownerUserId: form.ownerUserId,
        assistantUserId: form.assistantUserId,
        dueAt: form.dueAt,
        nextAction: form.nextAction
      } as TaskUpdateRequest);
      ElMessage.success("任务已更新");
    } else {
      await createTask({
        taskTitle: form.taskTitle,
        taskType: form.taskType,
        taskDescription: form.taskDescription,
        priority: form.priority || undefined,
        ownerUserId: form.ownerUserId,
        assistantUserId: form.assistantUserId,
        dueAt: form.dueAt
      } as TaskCreateRequest);
      ElMessage.success("任务已创建");
    }
    emit("saved");
    emit("close");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑任务' : '新增任务'"
    width="520px"
    :close-on-click-modal="false"
    @update:model-value="emit('close')"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="top"
    >
      <el-form-item label="任务标题" prop="taskTitle">
        <el-input v-model="form.taskTitle" placeholder="输入任务标题" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="任务类型">
            <el-select v-model="form.taskType" placeholder="选择" style="width: 100%" clearable>
              <el-option label="客户跟进" value="客户跟进" />
              <el-option label="内部事务" value="内部事务" />
              <el-option label="会议安排" value="会议安排" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-select v-model="form.priority" placeholder="选择" style="width: 100%" clearable>
              <el-option label="高" value="高" />
              <el-option label="中" value="中" />
              <el-option label="低" value="低" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="任务描述">
        <el-input
          v-model="form.taskDescription"
          type="textarea"
          :rows="2"
          placeholder="描述任务详情"
        />
      </el-form-item>

      <el-form-item label="截止时间">
        <el-date-picker
          v-model="form.dueAt"
          type="datetime"
          placeholder="选择截止时间"
          style="width: 100%"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
      </el-form-item>

      <el-form-item v-if="isEdit" label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="待开始" value="待开始" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="isEdit" label="下一步动作">
        <el-input v-model="form.nextAction" placeholder="下一步计划" />
      </el-form-item>

      <el-form-item label="负责人" prop="ownerUserId">
        <el-select v-model="form.ownerUserId" style="width: 100%">
          <el-option label="秦梓源" :value="1" />
          <el-option label="志明" :value="2" />
          <el-option label="哲涛" :value="3" />
          <el-option label="嘉诚" :value="4" />
          <el-option label="许润" :value="5" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('close')" :disabled="submitting">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ isEdit ? "保存修改" : "创建任务" }}
      </el-button>
    </template>
  </el-dialog>
</template>
