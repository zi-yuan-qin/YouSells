<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";

const props = defineProps<{
  modelValue: {
    category: string;
    keyword: string;
  };
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: { category: string; keyword: string }): void;
  (e: "search"): void;
}>();

const categoryOptions = ["全部", "邀约", "沟通", "跟进", "转化", "其他"];

function emitSearch() {
  emit("search");
}

function onCategoryChange(category: string) {
  const value = category === "全部" ? "" : category;
  emit("update:modelValue", { ...props.modelValue, category: value });
  emitSearch();
}

function onKeywordChange(keyword: string) {
  emit("update:modelValue", { ...props.modelValue, keyword });
}
</script>

<template>
  <div class="topic-filter-bar">
    <el-radio-group
      :model-value="modelValue.category || '全部'"
      size="default"
      @change="onCategoryChange"
    >
      <el-radio-button v-for="opt in categoryOptions" :key="opt" :label="opt" :value="opt">
        {{ opt }}
      </el-radio-button>
    </el-radio-group>

    <el-input
      :model-value="modelValue.keyword"
      :disabled="loading"
      placeholder="搜索问题标题"
      clearable
      style="width: 240px"
      @update:model-value="onKeywordChange"
      @keyup.enter="emitSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
  </div>
</template>

<style scoped>
.topic-filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
</style>
