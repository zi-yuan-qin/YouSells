<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import ScriptCategoryTabs from "@/components/script/ScriptCategoryTabs.vue";
import ScriptTable from "@/components/script/ScriptTable.vue";
import ScriptPreviewDrawer from "@/components/script/ScriptPreviewDrawer.vue";
import ScriptEditDialog from "@/components/script/ScriptEditDialog.vue";
import { fetchScriptCategories, fetchScripts } from "@/api/script";
import type { ScriptCategory, ScriptItem } from "@/types/script";

const loading = ref(false);
const categories = ref<ScriptCategory[]>([]);
const scripts = ref<ScriptItem[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);
const activeCategoryId = ref<number | null>(null);
const keyword = ref("");

const drawerVisible = ref(false);
const previewScript = ref<ScriptItem | null>(null);
const editDialogVisible = ref(false);
const editingScript = ref<ScriptItem | null>(null);

async function loadCategories() {
  try {
    categories.value = await fetchScriptCategories();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "话术分类加载失败");
  }
}

async function loadScripts(p = 1, ps = 20) {
  loading.value = true;
  try {
    const params: Record<string, unknown> = { page: p, pageSize: ps };
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value;
    if (keyword.value) params.keyword = keyword.value;
    const data = await fetchScripts(params as any);
    scripts.value = data.list;
    total.value = data.total;
    page.value = p;
    pageSize.value = ps;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "话术库加载失败");
  } finally {
    loading.value = false;
  }
}

function onCategoryChange(catId: number | null) {
  activeCategoryId.value = catId;
  void loadScripts(1, pageSize.value);
}

function onSearch(kw: string) {
  keyword.value = kw;
  void loadScripts(1, pageSize.value);
}

function onPageChange(p: number, ps: number) {
  void loadScripts(p, ps);
}

function openView(script: ScriptItem) {
  previewScript.value = script;
  drawerVisible.value = true;
}

function openCreate() {
  editingScript.value = null;
  editDialogVisible.value = true;
}

function openEdit(script: ScriptItem) {
  editingScript.value = script;
  editDialogVisible.value = true;
}

function onDrawerClose() {
  drawerVisible.value = false;
  previewScript.value = null;
}

function onEditDialogClose() {
  editDialogVisible.value = false;
  editingScript.value = null;
}

function onSaved() {
  void loadScripts(page.value, pageSize.value);
}

onMounted(() => {
  void loadCategories();
  void loadScripts();
});
</script>

<template>
  <div class="page-shell">
    <PageSection
      title="话术库"
      description="分类管理、话术沉淀与快速检索"
    >
      <ScriptCategoryTabs
        :categories="categories"
        :active-category-id="activeCategoryId"
        @change="onCategoryChange"
      />

      <ScriptTable
        :scripts="scripts"
        :loading="loading"
        :total="total"
        :page="page"
        :page-size="pageSize"
        @page-change="onPageChange"
        @view="openView"
        @edit="openEdit"
        @create="openCreate"
        @search="onSearch"
      />
    </PageSection>

    <ScriptPreviewDrawer
      :visible="drawerVisible"
      :script="previewScript"
      :loading="false"
      @close="onDrawerClose"
      @edit="(s) => { onDrawerClose(); openEdit(s); }"
    />

    <ScriptEditDialog
      :visible="editDialogVisible"
      :script="editingScript"
      :categories="categories"
      @close="onEditDialogClose"
      @saved="onSaved"
    />
  </div>
</template>
