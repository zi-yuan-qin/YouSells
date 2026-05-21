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
  followType: "",
  communicatedContent: "",
  customerFeedback: null,
  currentConcern: null,
  nextAction: null,
  nextFollowAt: null
});

const formRef = ref();

const rules = {
  followType: [{ required: true, message: "请选择跟进方式", trigger: "change" }],
  communicatedContent: [{ required: true, message: "请填写沟通内容", trigger: "blur" }]
};

function open() {
  form.customerId = props.customerId;
  form.followType = "";
  form.communicatedContent = "";
  form.customerFeedback = null;
  form.currentConcern = null;
  form.nextAction = null;
  form.nextFollowAt = null;
  visible.value = true;
}

function close() {
  visible.value = false;
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
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
      width="560px"
      :close-on-click-modal="false"
      @closed="close"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="top"
      >
        <el-form-item label="跟进方式" prop="followType">
          <el-select v-model="form.followType" placeholder="选择跟进方式" style="width: 100%">
            <el-option label="电话" value="电话" />
            <el-option label="微信" value="微信" />
            <el-option label="面谈" value="面谈" />
            <el-option label="邮件" value="邮件" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="沟通内容" prop="communicatedContent">
          <el-input
            v-model="form.communicatedContent"
            type="textarea"
            :rows="3"
            placeholder="记录本次沟通的核心内容"
          />
        </el-form-item>

        <el-form-item label="客户反馈">
          <el-input
            v-model="form.customerFeedback"
            type="textarea"
            :rows="2"
            placeholder="客户说了什么、态度如何"
          />
        </el-form-item>

        <el-form-item label="当前顾虑">
          <el-input
            v-model="form.currentConcern"
            placeholder="客户当前的顾虑点"
          />
        </el-form-item>

        <el-form-item label="下一步动作">
          <el-input
            v-model="form.nextAction"
            placeholder="下一步计划做什么"
          />
        </el-form-item>

        <el-form-item label="计划跟进时间">
          <el-date-picker
            v-model="form.nextFollowAt"
            type="datetime"
            placeholder="选择时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
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
