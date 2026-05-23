<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";

const props = defineProps<{
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "submit", data: { content: string }): void;
}>();

const content = ref("");

function handleSubmit() {
  const trimmed = content.value.trim();
  if (!trimmed) {
    ElMessage.warning("请输入方案内容");
    return;
  }
  emit("submit", { content: trimmed });
  content.value = "";
}
</script>

<template>
  <div class="reply-form">
    <h4 class="reply-form__title">写方案</h4>
    <el-input
      v-model="content"
      type="textarea"
      :rows="4"
      placeholder="写出你的方案或建议..."
    />
    <div class="reply-form__actions">
      <el-button type="primary" :loading="props.loading" @click="handleSubmit">
        提交方案
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.reply-form {
  margin-top: 8px;
}

.reply-form__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.reply-form__actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
