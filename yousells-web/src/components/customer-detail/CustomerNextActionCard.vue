<script setup lang="ts">
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import type { CustomerDetail, CustomerNextFollowRequest } from "@/types/customer-detail";
import { updateNextFollow } from "@/api/customer-detail";

const props = defineProps<{
  detail: CustomerDetail;
  loading: boolean;
}>();

const emit = defineEmits<{
  updated: [];
}>();

const editing = ref(false);
const saving = ref(false);
const form = ref<CustomerNextFollowRequest>({
  nextAction: props.detail.nextFollowAction || "",
  nextFollowAt: props.detail.nextFollowAt || ""
});

watch(
  () => props.detail,
  (d) => {
    if (!editing.value) {
      form.value = {
        nextAction: d.nextFollowAction || "",
        nextFollowAt: d.nextFollowAt || ""
      };
    }
  }
);

function isPast(dateStr: string): boolean {
  if (!dateStr) return false;
  return new Date(dateStr).getTime() < Date.now();
}

function startEdit() {
  form.value = {
    nextAction: props.detail.nextFollowAction || "",
    nextFollowAt: props.detail.nextFollowAt || ""
  };
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
}

async function save() {
  if (!form.value.nextAction || !form.value.nextFollowAt) {
    ElMessage.warning("请填写下一次跟进动作和计划时间");
    return;
  }
  saving.value = true;
  try {
    await updateNextFollow(props.detail.id, {
      nextAction: form.value.nextAction,
      nextFollowAt: form.value.nextFollowAt
    });
    editing.value = false;
    emit("updated");
    ElMessage.success("下一次跟进已更新");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "更新失败");
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="next-action-card" v-loading="loading">
    <div class="next-action-card__header">
      <h3 class="page-section__title" style="font-size: 18px; margin: 0">
        下一次跟进
      </h3>
      <div class="next-action-card__actions">
        <template v-if="!editing">
          <el-button size="small" @click="startEdit">编辑</el-button>
        </template>
        <template v-else>
          <el-button size="small" @click="cancelEdit" :disabled="saving">取消</el-button>
          <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
        </template>
      </div>
    </div>

    <div v-if="!editing" class="next-action-card__view">
      <div class="next-action-row">
        <span class="next-action-row__label">跟进动作</span>
        <span class="next-action-row__value">{{ detail.nextFollowAction || "暂无" }}</span>
      </div>
      <div class="next-action-row">
        <span class="next-action-row__label">计划时间</span>
        <span
          class="next-action-row__value"
          :class="{ 'text-danger': detail.nextFollowAt && isPast(detail.nextFollowAt) }"
        >
          {{ detail.nextFollowAt || "暂无" }}
        </span>
      </div>
    </div>

    <div v-else class="next-action-card__form">
      <el-input
        v-model="form.nextAction"
        placeholder="下一次跟进动作，如：电话回访、发送方案"
        size="small"
        style="margin-bottom: 12px"
      />
      <el-date-picker
        v-model="form.nextFollowAt"
        type="datetime"
        placeholder="选择计划跟进时间"
        size="small"
        style="width: 100%"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DDTHH:mm:ss"
      />
    </div>
  </div>
</template>

<style scoped>
.next-action-card {
  background: #fafcff;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid rgba(37, 99, 235, 0.06);
}

.next-action-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.next-action-card__actions {
  display: flex;
  gap: 8px;
}

.next-action-card__view {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.next-action-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.next-action-row__label {
  color: #64748b;
  min-width: 80px;
}

.next-action-row__value {
  color: #172033;
  font-weight: 600;
}

.text-danger {
  color: #e53e3e;
}
</style>
