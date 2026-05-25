<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Plus, More, Upload, Document, Download, Refresh } from "@element-plus/icons-vue";
import PageSection from "@/components/app/PageSection.vue";
import CustomerFilterBar from "@/components/customer-list/CustomerFilterBar.vue";
import CustomerTable from "@/components/customer-list/CustomerTable.vue";
import CustomerCreateDialog from "@/components/customer-list/CustomerCreateDialog.vue";
import { fetchCustomers, createCustomer, importCustomers, exportCustomers, downloadImportTemplate } from "@/api/customer-list";
import type { CustomerListItem, CustomerQuery, CustomerCreateRequest } from "@/types/customer-list";
import { RouteName } from "@/router/route-names";
import { isUnauthorizedError } from "@/utils/request-error";

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const error = ref(false);
const customers = ref<CustomerListItem[]>([]);
const total = ref(0);
const createDialogVisible = ref(false);
const createLoading = ref(false);
const importLoading = ref(false);

const query = reactive<CustomerQuery>({
  page: 1,
  pageSize: 20,
  keyword: ""
});

async function loadCustomers() {
  error.value = false;
  loading.value = true;
  try {
    const data = await fetchCustomers(query);
    customers.value = data.list;
    total.value = data.total;
  } catch (e) {
    if (isUnauthorizedError(e)) {
      return;
    }
    error.value = true;
    ElMessage.error(e instanceof Error ? e.message : "客户列表加载失败");
  } finally {
    loading.value = false;
  }
}

function buildUrlQuery() {
  const q: Record<string, string> = {};
  if (query.keyword) q.keyword = query.keyword;
  if (query.grade) q.grade = query.grade;
  if (query.progress) q.progress = query.progress;
  if (query.intent) q.intent = query.intent;
  if (query.page && query.page > 1) q.page = String(query.page);
  if (query.pageSize && query.pageSize !== 20) q.pageSize = String(query.pageSize);
  return q;
}

function syncUrl() {
  router.replace({ query: buildUrlQuery() });
}

function restoreFromUrl() {
  const { keyword, grade, progress, intent, page, pageSize } = route.query;
  query.keyword = keyword ? String(keyword) : undefined;
  query.grade = grade ? String(grade) : undefined;
  query.progress = progress ? String(progress) : undefined;
  query.intent = intent ? String(intent) : undefined;
  query.page = page ? Number(page) : 1;
  query.pageSize = pageSize ? Number(pageSize) : 20;
}

function onSearch() {
  query.page = 1;
  syncUrl();
  void loadCustomers();
}

function onReset() {
  query.page = 1;
  query.keyword = undefined;
  query.grade = undefined;
  query.progress = undefined;
  query.intent = undefined;
  syncUrl();
  void loadCustomers();
}

function onPageChange(page: number) {
  query.page = page;
  syncUrl();
  void loadCustomers();
}

async function onCreateSubmit(data: CustomerCreateRequest) {
  createLoading.value = true;
  try {
    await createCustomer(data);
    ElMessage.success("客户创建成功");
    createDialogVisible.value = false;
    query.page = 1;
    await loadCustomers();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "创建失败");
  } finally {
    createLoading.value = false;
  }
}

async function onImport(file: File) {
  importLoading.value = true;
  try {
    const msg = await importCustomers(file);
    ElMessage.success(msg);
    await loadCustomers();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "导入失败");
  } finally {
    importLoading.value = false;
  }
}

async function onExport() {
  try {
    const blob = await exportCustomers(query);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '客户列表.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '导出失败');
  }
}

async function onDownloadTemplate() {
  try {
    const blob = await downloadImportTemplate();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '客户导入模板.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '模板下载失败');
  }
}

function onRowClick(row: CustomerListItem) {
  router.push({ name: RouteName.CustomerDetail, params: { id: row.id } });
}

function onQueryUpdate(v: CustomerQuery) {
  Object.assign(query, v);
}

onMounted(() => {
  restoreFromUrl();
  void loadCustomers();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="客户管理"
      description="学生客户总表，支持按年级/进度/意向筛选"
    >
      <CustomerFilterBar :model-value="query" :loading="loading" @update:model-value="onQueryUpdate" @search="onSearch" @reset="onReset" />

      <div class="customer-toolbar">
        <el-button type="primary" @click="createDialogVisible = true">
          <el-icon><Plus /></el-icon> 新建客户
        </el-button>

        <el-dropdown trigger="click">
          <el-button text>
            <el-icon><More /></el-icon> 更多
          </el-button>
          <template #dropdown>
            <el-dropdown-item>
              <el-upload
                accept=".xlsx,.xls"
                :show-file-list="false"
                :disabled="importLoading"
                :before-upload="(file: File) => { onImport(file); return false; }"
                style="display: inline"
              >
                <span style="display: flex; align-items: center; gap: 6px;" :class="{ 'is-disabled': importLoading }">
                  <el-icon v-if="importLoading" class="is-loading"><Upload /></el-icon>
                  <el-icon v-else><Upload /></el-icon>
                  {{ importLoading ? '导入中...' : '批量导入' }}
                </span>
              </el-upload>
            </el-dropdown-item>
            <el-dropdown-item @click="onDownloadTemplate">
              <el-icon><Document /></el-icon> 下载导入模板
            </el-dropdown-item>
            <el-dropdown-item @click="onExport">
              <el-icon><Download /></el-icon> 导出Excel
            </el-dropdown-item>
            <el-dropdown-item divided @click="loadCustomers">
              <el-icon><Refresh /></el-icon> 刷新列表
            </el-dropdown-item>
          </template>
        </el-dropdown>
      </div>

      <div v-if="error" class="dashboard-error">
        <p>数据加载失败，请点击刷新重试</p>
      </div>

      <CustomerTable
        :customers
        :loading
        :total
        :page="query.page ?? 1"
        :page-size="query.pageSize ?? 20"
        @page-change="onPageChange"
        @row-click="onRowClick"
        @export="onExport"
        @refresh="loadCustomers"
      />
    </PageSection>

    <CustomerCreateDialog
      v-model:visible="createDialogVisible"
      :loading="createLoading"
      @submit="onCreateSubmit"
    />
  </div>
</template>

<style scoped>
.customer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
