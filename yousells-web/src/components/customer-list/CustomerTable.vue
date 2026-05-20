<script setup lang="ts">
import type { CustomerListItem } from "@/types/customer-list";

defineProps<{
  customers: CustomerListItem[];
  loading: boolean;
  total: number;
  page: number;
  pageSize: number;
}>();

const emit = defineEmits<{
  (e: "page-change", page: number): void;
  (e: "row-click", row: CustomerListItem): void;
}>();
</script>

<template>
  <div class="customer-table-wrapper">
    <el-table
      :data="customers"
      border
      stripe
      v-loading="loading"
      highlight-current-row
      @row-click="(row: CustomerListItem) => emit('row-click', row)"
    >
      <el-table-column prop="customerCode" label="客户编号" min-width="150" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="sourcePlatform" label="来源平台" min-width="110" />
      <el-table-column prop="intentLevel" label="意向等级" width="100" />
      <el-table-column prop="currentStage" label="当前阶段" min-width="140" />
      <el-table-column prop="ownerDisplayName" label="负责人" width="110" />
      <el-table-column prop="nextFollowAt" label="下次跟进" min-width="170" />
      <el-table-column label="标签" min-width="150">
        <template #default="{ row }">
          <template v-if="row.tags.length">
            <el-tag
              v-for="tag in row.tags"
              :key="tag"
              size="small"
              effect="plain"
              style="margin-right: 4px"
            >
              {{ tag }}
            </el-tag>
          </template>
          <span v-else class="tag-none">无</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="(p: number) => emit('page-change', p)"
    />
  </div>
</template>
