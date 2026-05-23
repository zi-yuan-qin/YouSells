<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import type { WeeklyReportCreateRequest } from "@/types/report";
import { createWeeklyReport } from "@/api/report";

const props = defineProps<{
  weekKey?: string;
}>();

const emit = defineEmits<{
  submitted: [];
}>();

const submitting = ref(false);
const formRef = ref();

const form = reactive<WeeklyReportCreateRequest>({
  weekKey: props.weekKey || currentWeekKey(),
  summary: "",
  issues: null,
  nextWeekPlan: ""
});

const rules = {
  weekKey: [{ required: true, message: "请填写周标识", trigger: "blur" }],
  summary: [{ required: true, message: "请填写本周工作总结", trigger: "blur" }],
  nextWeekPlan: [{ required: true, message: "请填写下周计划", trigger: "blur" }]
};

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    await createWeeklyReport({
      weekKey: form.weekKey,
      summary: form.summary,
      issues: form.issues || null,
      nextWeekPlan: form.nextWeekPlan
    });
    form.weekKey = "";
    form.summary = "";
    form.issues = null;
    form.nextWeekPlan = "";
    emit("submitted");
    ElMessage.success("周报已提交");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "提交失败");
  } finally {
    submitting.value = false;
  }
}

function currentWeekKey(): string {
  const now = new Date();
  const year = now.getFullYear();
  const start = new Date(year, 0, 1);
  const days = Math.floor((now.getTime() - start.getTime()) / (24 * 60 * 60 * 1000));
  const week = Math.ceil((days + start.getDay() + 1) / 7);
  return `${year}-W${String(week).padStart(2, "0")}`;
}
</script>

<template>
  <div class="report-form-card">
    <h3 class="report-form-card__title">提交周报</h3>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="top"
    >
      <el-form-item label="周标识" prop="weekKey">
        <el-input
          v-model="form.weekKey"
          :placeholder="'如 ' + currentWeekKey()"
        />
      </el-form-item>

      <el-form-item label="本周总结" prop="summary">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="本周完成了哪些重点工作"
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

      <el-form-item label="下周计划" prop="nextWeekPlan">
        <el-input
          v-model="form.nextWeekPlan"
          type="textarea"
          :rows="2"
          placeholder="下周计划推进哪些工作"
        />
      </el-form-item>

      <el-button
        type="primary"
        :loading="submitting"
        @click="submit"
      >
        提交周报
      </el-button>
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
