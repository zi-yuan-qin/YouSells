<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import type { FollowUpCreateRequest } from "@/types/followup";
import { createFollowUp } from "@/api/followup";

const props = defineProps<{
  customerId: number;
}>();

const emit = defineEmits<{
  created: [];
}>();

const submitting = ref(false);
const visible = ref(false);

const form = reactive<FollowUpCreateRequest>({
  customerId: props.customerId,
  progress: "",
  content: "",
  feedback: null,
  nextAction: null
});

const formRef = ref();

const rules = {
  progress: [{ required: true, message: "请选择当前进度", trigger: "change" }],
  content: [{ required: true, message: "请填写沟通内容", trigger: "blur" }]
};

function open() {
  form.customerId = props.customerId;
  form.progress = "";
  form.content = "";
  form.feedback = null;
  form.nextAction = null;
  visible.value = true;
}

function close() {
  visible.value = false;
}

async function submit() {
  const valid = !formRef.value ? false : await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    await createFollowUp({ ...form, customerId: props.customerId });
    visible.value = false;
    emit("created");
    ElMessage.success("跟进记录已添加");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "添加跟进记录失败");
  } finally {
    submitting.value = false;
  }
}

defineExpose({ open });
</script>

<template>
  <div class="followup-form-trigger">
    <el-button type="primary" @click="open">新增跟进记录</el-button>

    <el-dialog
      v-model="visible"
      title="新增跟进记录"
      width="500px"
      :close-on-click-modal="false"
      @closed="close"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="top"
      >
        <el-form-item label="当前进度" prop="progress">
          <el-select v-model="form.progress" placeholder="选择本次沟通时的进度" style="width: 100%">
            <el-option label="职规" value="职规" />
            <el-option label="技术栈" value="技术栈" />
            <el-option label="课程" value="课程" />
          </el-select>
        </el-form-item>

        <el-form-item label="沟通内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="记录本次沟通的核心内容"
          />
        </el-form-item>

        <el-form-item label="学生反馈">
          <el-input
            v-model="form.feedback"
            type="textarea"
            :rows="2"
            placeholder="学生说了什么、态度如何"
          />
        </el-form-item>

        <el-form-item label="下一步动作">
          <el-input
            v-model="form.nextAction"
            placeholder="下一步计划做什么"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="close" :disabled="submitting">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          提交跟进记录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.followup-form-trigger {
  margin-bottom: 12px;
}
</style>
