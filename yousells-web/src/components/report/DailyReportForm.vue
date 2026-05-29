<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import type { DailyReport, DailyReportCreateRequest } from "@/types/report";
import { createDailyReport, updateDailyReport } from "@/api/report";

const props = defineProps<{
  editReport?: DailyReport | null;
}>();

const emit = defineEmits<{
  submitted: [];
  updated: [];
  cancel: [];
}>();

const submitting = ref(false);
const formRef = ref();
const isEditing = ref(false);

const form = reactive<DailyReportCreateRequest>({
  reportDate: new Date().toISOString().slice(0, 10),
  summary: "",
  issues: null,
  tomorrowPlan: ""
});

watch(() => props.editReport, (report) => {
  if (report) {
    isEditing.value = true;
    form.reportDate = report.reportDate;
    form.summary = report.summary;
    form.issues = report.issues;
    form.tomorrowPlan = report.tomorrowPlan;
  }
}, { immediate: true });

const rules = {
  reportDate: [{ required: true, message: "请选择日期", trigger: "change" }],
  summary: [{ required: true, message: "请填写今日工作小结", trigger: "blur" }],
  tomorrowPlan: [{ required: true, message: "请填写明日计划", trigger: "blur" }]
};

async function submit() {
  const valid = !formRef.value ? false : await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (isEditing.value && props.editReport) {
      await updateDailyReport(props.editReport.id, {
        reportDate: form.reportDate,
        summary: form.summary,
        issues: form.issues || null,
        tomorrowPlan: form.tomorrowPlan
      });
      ElMessage.success("日报已更新");
      emit("updated");
    } else {
      await createDailyReport({
        reportDate: form.reportDate,
        summary: form.summary,
        issues: form.issues || null,
        tomorrowPlan: form.tomorrowPlan
      });
      form.summary = "";
      form.issues = null;
      form.tomorrowPlan = "";
      emit("submitted");
      ElMessage.success("日报已提交");
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : (isEditing.value ? "更新失败" : "提交失败"));
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  isEditing.value = false;
  emit("cancel");
}
</script>

<template>
  <div class="report-form-card">
    <h3 class="report-form-card__title">{{ isEditing ? '编辑日报' : '提交日报' }}</h3>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="top"
    >
      <el-form-item label="日期" prop="reportDate">
        <el-date-picker
          v-model="form.reportDate"
          type="date"
          placeholder="选择日期"
          style="width: 100%"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="今日小结" prop="summary">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="今天完成了哪些工作"
        />
      </el-form-item>

      <el-form-item label="遇到的问题">
        <el-input
          v-model="form.issues"
          type="textarea"
          :rows="2"
          placeholder="遇到了什么问题或困难"
        />
      </el-form-item>

      <el-form-item label="明日计划" prop="tomorrowPlan">
        <el-input
          v-model="form.tomorrowPlan"
          type="textarea"
          :rows="2"
          placeholder="明天计划做什么"
        />
      </el-form-item>

      <el-button
        type="primary"
        :loading="submitting"
        @click="submit"
      >
        {{ isEditing ? '保存修改' : '提交日报' }}
      </el-button>
      <el-button v-if="isEditing" @click="handleCancel">取消</el-button>
    </el-form>
  </div>
</template>

<style scoped>
.report-form-card {
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.report-form-card__title {
  margin: 0 0 16px;
  font-size: 18px;
}
</style>
