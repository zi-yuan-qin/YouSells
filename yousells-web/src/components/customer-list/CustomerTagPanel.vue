<script setup lang="ts">
import { onMounted, ref } from "vue";
import { fetchCustomerTags } from "@/api/customer-list";
import type { CustomerTag } from "@/types/customer-list";

const emit = defineEmits<{
  (e: "tag-click", tag: CustomerTag): void;
}>();

const tags = ref<CustomerTag[]>([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    tags.value = await fetchCustomerTags();
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="tag-panel">
    <h4>客户标签</h4>
    <p v-if="loading" class="tag-panel__placeholder">加载中...</p>
    <p v-else-if="tags.length === 0" class="tag-panel__placeholder">暂无标签</p>
    <div v-else class="tag-panel__list">
      <el-tag
        v-for="tag in tags"
        :key="tag.id"
        :color="tag.tagColor ?? undefined"
        size="default"
        effect="dark"
        class="tag-panel__tag"
        style="cursor: pointer"
        @click="emit('tag-click', tag)"
      >
        {{ tag.tagName }}
      </el-tag>
    </div>
  </div>
</template>
