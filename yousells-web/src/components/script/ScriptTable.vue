<script setup lang="ts">
import { ref } from "vue";
import type { ScriptItem } from "@/types/script";

defineProps<{
  scripts: ScriptItem[];
  loading: boolean;
  total: number;
  page: number;
  pageSize: number;
}>();

const emit = defineEmits<{
  "page-change": [page: number, pageSize: number];
  view: [script: ScriptItem];
  edit: [script: ScriptItem];
  create: [];
  search: [keyword: string];
}>();

const keyword = ref("");

function onSearch() {
  emit("search", keyword.value);
}

function onClear() {
  keyword.value = "";
  emit("search", "");
}
</script>

<template>
  <div class="script-toolbar">
    <div class="script-toolbar__search">
      <el-input
        v-model="keyword"
        placeholder="搜索话术标题或内容"
        size="small"
        clearable
        style="width: 260px"
        @keyup.enter="onSearch"
        @clear="onClear"
      >
        <template #append>
          <el-button @click="onSearch">搜索</el-button>
        </template>
      </el-input>
    </div>
    <el-button type="primary" size="small" @click="emit('create')">新增话术</el-button>
  </div>

  <el-table
    :data="scripts"
    v-loading="loading"
    border
    stripe
    size="small"
  >
    <el-table-column prop="categoryName" label="分类" min-width="120" />
    <el-table-column prop="title" label="标题" min-width="180" />
    <el-table-column prop="applicableScene" label="适用场景" min-width="200">
      <template #default="{ row }">
        {{ row.applicableScene || "—" }}
      </template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="90">
      <template #default="{ row }">
        <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">
          {{ row.status }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
    <el-table-column label="操作" width="120" fixed="right">
      <template #default="{ row }">
        <el-button size="small" text type="primary" @click="emit('view', row)">查看</el-button>
        <el-button size="small" text @click="emit('edit', row)">编辑</el-button>
      </template>
    </el-table-column>
  </el-table>

  <div class="script-table__pagination" v-if="total > 0">
    <el-pagination
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, prev, pager, next, sizes"
      small
      @update:current-page="emit('page-change', $event, pageSize)"
      @update:page-size="emit('page-change', page, $event)"
    />
  </div>
</template>

<style scoped>
.script-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
}

.script-table__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
