<script setup lang="ts">
import type { ScriptCategory } from "@/types/script";

defineProps<{
  categories: ScriptCategory[];
  activeCategoryId: number | null;
}>();

const emit = defineEmits<{
  change: [categoryId: number | null];
}>();
</script>

<template>
  <div class="category-tabs">
    <el-tag
      :type="activeCategoryId === null ? 'primary' : 'info'"
      :effect="activeCategoryId === null ? 'dark' : 'plain'"
      class="category-tab"
      @click="emit('change', null)"
    >
      全部
    </el-tag>
    <el-tag
      v-for="cat in categories"
      :key="cat.id"
      :type="activeCategoryId === cat.id ? 'primary' : 'info'"
      :effect="activeCategoryId === cat.id ? 'dark' : 'plain'"
      class="category-tab"
      @click="emit('change', cat.id)"
    >
      {{ cat.categoryName }}
    </el-tag>
  </div>
</template>

<style scoped>
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.category-tab {
  cursor: pointer;
  transition: transform 0.15s;
}

.category-tab:hover {
  transform: translateY(-1px);
}
</style>
