<script setup lang="ts">
import type { CustomerQuery } from "@/types/customer-list";

const props = defineProps<{
  modelValue: CustomerQuery;
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: CustomerQuery): void;
  (e: "search"): void;
  (e: "reset"): void;
}>();

const intentOptions = [
  { label: "全部", value: "" },
  { label: "A", value: "A" },
  { label: "B", value: "B" },
  { label: "C", value: "C" },
  { label: "D", value: "D" }
];

const stageOptions = [
  { label: "全部", value: "" },
  { label: "首轮沟通", value: "FIRST_COMMUNICATION" },
  { label: "培育中", value: "NURTURING" },
  { label: "高意向", value: "HIGH_INTENT" },
  { label: "待成交", value: "PENDING_CLOSE" },
  { label: "已成交", value: "CONVERTED" },
  { label: "暂停", value: "PAUSED" },
  { label: "转考", value: "TRANSFER_TO_EXAM" }
];

function onSearch() {
  emit("update:modelValue", { ...props.modelValue, page: 1 });
  emit("search");
}

function onReset() {
  const resetQuery: CustomerQuery = { page: 1, pageSize: props.modelValue.pageSize ?? 20 };
  emit("update:modelValue", resetQuery);
  emit("reset");
}
</script>

<template>
  <div class="filter-bar">
    <el-input
      :model-value="modelValue.keyword ?? ''"
      :disabled="loading"
      placeholder="搜索客户昵称/编号"
      clearable
      style="width: 200px"
      @update:model-value="emit('update:modelValue', { ...modelValue, keyword: $event || undefined })"
      @keyup.enter="onSearch"
    />
    <el-select
      :model-value="modelValue.intentLevel ?? ''"
      :disabled="loading"
      placeholder="意向等级"
      style="width: 130px"
      @update:model-value="emit('update:modelValue', { ...modelValue, intentLevel: $event || undefined }); onSearch()"
    >
      <el-option v-for="opt in intentOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
    </el-select>
    <el-select
      :model-value="modelValue.currentStage ?? ''"
      :disabled="loading"
      placeholder="当前阶段"
      style="width: 150px"
      @update:model-value="emit('update:modelValue', { ...modelValue, currentStage: $event || undefined }); onSearch()"
    >
      <el-option v-for="opt in stageOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
    </el-select>
    <el-input
      :model-value="modelValue.sourcePlatform ?? ''"
      :disabled="loading"
      placeholder="来源平台"
      clearable
      style="width: 160px"
      @update:model-value="emit('update:modelValue', { ...modelValue, sourcePlatform: $event || undefined })"
      @keyup.enter="onSearch"
    />
    <el-button type="primary" :loading="loading" @click="onSearch">搜索</el-button>
    <el-button :disabled="loading" @click="onReset">重置</el-button>
  </div>
</template>
