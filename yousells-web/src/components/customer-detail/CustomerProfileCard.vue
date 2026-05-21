<script setup lang="ts">
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import type { CustomerDetail, CustomerUpdateRequest } from "@/types/customer-detail";
import { updateCustomer } from "@/api/customer-detail";

const props = defineProps<{
  detail: CustomerDetail;
  loading: boolean;
}>();

const emit = defineEmits<{
  updated: [];
}>();

const editing = ref(false);
const saving = ref(false);
const form = ref<CustomerUpdateRequest>(buildForm(props.detail));

function buildForm(d: CustomerDetail): CustomerUpdateRequest {
  return {
    nickname: d.nickname,
    contactValue: d.contactValue,
    sourcePlatform: d.sourcePlatform,
    expectedMajor: d.expectedMajor,
    baseLevel: d.baseLevel,
    intentLevel: d.intentLevel,
    currentStage: d.currentStage,
    currentConcern: d.currentConcern,
    latestFeedback: d.latestFeedback,
    ownerUserId: 1,
    assistantUserId: null,
    remarks: d.remarks
  };
}

watch(
  () => props.detail,
  (d) => {
    if (!editing.value) {
      form.value = buildForm(d);
    }
  }
);

function startEdit() {
  form.value = buildForm(props.detail);
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
  form.value = buildForm(props.detail);
}

async function saveEdit() {
  saving.value = true;
  try {
    await updateCustomer(props.detail.id, form.value);
    editing.value = false;
    emit("updated");
    ElMessage.success("客户信息已更新");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "更新失败");
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="profile-card">
    <div class="profile-card__header">
      <h3 class="page-section__title" style="font-size: 18px; margin: 0">
        客户基础信息
      </h3>
      <div class="profile-card__actions">
        <template v-if="!editing">
          <el-button size="small" @click="startEdit">编辑</el-button>
        </template>
        <template v-else>
          <el-button size="small" @click="cancelEdit" :disabled="saving">取消</el-button>
          <el-button size="small" type="primary" :loading="saving" @click="saveEdit">
            保存
          </el-button>
        </template>
      </div>
    </div>

    <div v-loading="loading || saving" class="detail-grid">
      <div class="detail-item">
        <div class="detail-item__label">客户昵称</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.nickname }}</div>
        <el-input v-else v-model="form.nickname" size="small" />
      </div>
      <div class="detail-item">
        <div class="detail-item__label">联系方式</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.contactValue }}</div>
        <el-input v-else v-model="form.contactValue" size="small" />
      </div>
      <div class="detail-item">
        <div class="detail-item__label">来源平台</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.sourcePlatform }}</div>
        <el-input v-else v-model="form.sourcePlatform" size="small" />
      </div>
      <div class="detail-item">
        <div class="detail-item__label">意向等级</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.intentLevel }}</div>
        <el-select v-else v-model="form.intentLevel" size="small" style="width: 100%">
          <el-option label="高" value="高" />
          <el-option label="中" value="中" />
          <el-option label="低" value="低" />
        </el-select>
      </div>
      <div class="detail-item">
        <div class="detail-item__label">当前阶段</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.currentStage }}</div>
        <el-select v-else v-model="form.currentStage" size="small" style="width: 100%">
          <el-option label="待跟进" value="待跟进" />
          <el-option label="跟进中" value="跟进中" />
          <el-option label="待成交" value="待成交" />
          <el-option label="已成交" value="已成交" />
          <el-option label="已流失" value="已流失" />
        </el-select>
      </div>
      <div class="detail-item">
        <div class="detail-item__label">意向方向</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.interestDirection || "暂无" }}</div>
        <el-input v-else v-model="form.baseLevel" size="small" />
      </div>
      <div class="detail-item">
        <div class="detail-item__label">基础等级</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.baseLevel || "暂无" }}</div>
        <el-select v-else v-model="form.baseLevel" size="small" style="width: 100%" clearable>
          <el-option label="A" value="A" />
          <el-option label="B" value="B" />
          <el-option label="C" value="C" />
        </el-select>
      </div>
      <div class="detail-item">
        <div class="detail-item__label">期望专业</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.expectedMajor || "暂无" }}</div>
        <el-input v-else v-model="form.expectedMajor" size="small" />
      </div>
      <div class="detail-item" style="grid-column: 1 / -1">
        <div class="detail-item__label">备注</div>
        <div class="detail-item__value" v-if="!editing">{{ detail.remarks || "暂无" }}</div>
        <el-input
          v-else
          v-model="form.remarks"
          type="textarea"
          :rows="2"
          size="small"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-card {
  background: #fafcff;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid rgba(37, 99, 235, 0.06);
}

.profile-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.profile-card__actions {
  display: flex;
  gap: 8px;
}
</style>
