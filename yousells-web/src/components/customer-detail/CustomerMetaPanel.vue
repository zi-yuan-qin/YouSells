<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { CustomerDetail, CustomerTagVo } from "@/types/customer-detail";
import { fetchCustomerTags, updateCustomerTags } from "@/api/customer-detail";

const props = defineProps<{
  detail: CustomerDetail;
  loading: boolean;
}>();

const emit = defineEmits<{
  "tags-updated": [];
}>();

const allTags = ref<CustomerTagVo[]>([]);
const editingTags = ref(false);
const savingTags = ref(false);
const selectedTagIds = ref<number[]>([]);

async function loadTags() {
  try {
    allTags.value = await fetchCustomerTags();
  } catch {
    // silent
  }
}

function startTagEdit() {
  selectedTagIds.value = [];
  editingTags.value = true;
}

function cancelTagEdit() {
  editingTags.value = false;
}

async function saveTags() {
  savingTags.value = true;
  try {
    await updateCustomerTags(props.detail.id, selectedTagIds.value);
    editingTags.value = false;
    emit("tags-updated");
    ElMessage.success("标签已更新");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "标签更新失败");
  } finally {
    savingTags.value = false;
  }
}

function toggleTag(tagId: number) {
  const idx = selectedTagIds.value.indexOf(tagId);
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1);
  } else {
    selectedTagIds.value.push(tagId);
  }
}

onMounted(() => {
  void loadTags();
});
</script>

<template>
  <div class="meta-panel" v-loading="loading">
    <div class="meta-section">
      <div class="meta-section__header">
        <span class="meta-section__label">客户标签</span>
        <template v-if="!editingTags">
          <el-button size="small" text @click="startTagEdit">编辑标签</el-button>
        </template>
        <template v-else>
          <el-button size="small" text @click="cancelTagEdit" :disabled="savingTags">取消</el-button>
          <el-button size="small" type="primary" text :loading="savingTags" @click="saveTags">
            保存
          </el-button>
        </template>
      </div>
      <div class="meta-section__body">
        <template v-if="!editingTags">
          <el-tag
            v-for="tag in detail.tags"
            :key="tag"
            effect="plain"
            size="small"
            class="meta-tag"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!detail.tags || detail.tags.length === 0" class="meta-empty">暂无标签</span>
        </template>
        <template v-else>
          <el-checkbox-group v-model="selectedTagIds" size="small">
            <el-checkbox
              v-for="tag in allTags"
              :key="tag.id"
              :label="tag.id"
              :value="tag.id"
              border
              size="small"
              class="tag-checkbox"
            >
              {{ tag.tagName }}
            </el-checkbox>
          </el-checkbox-group>
        </template>
      </div>
    </div>

    <div class="meta-section">
      <div class="meta-section__label">归属信息</div>
      <div class="meta-section__body">
        <div class="meta-row">
          <span class="meta-row__key">负责人</span>
          <span class="meta-row__value">{{ detail.ownerDisplayName }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">协助人</span>
          <span class="meta-row__value">{{ detail.assistantDisplayName || "暂无" }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">客户编号</span>
          <span class="meta-row__value">{{ detail.customerCode }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">客户类型</span>
          <span class="meta-row__value">{{ detail.customerType }}</span>
        </div>
      </div>
    </div>

    <div class="meta-section">
      <div class="meta-section__label">时间信息</div>
      <div class="meta-section__body">
        <div class="meta-row">
          <span class="meta-row__key">最后联系</span>
          <span class="meta-row__value">{{ detail.lastContactAt || "暂无" }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">下次跟进</span>
          <span class="meta-row__value">{{ detail.nextFollowAt || "暂无" }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">当前顾虑</span>
          <span class="meta-row__value">{{ detail.currentConcern || "暂无" }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-row__key">最新反馈</span>
          <span class="meta-row__value">{{ detail.latestFeedback || "暂无" }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.meta-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #fafcff;
  border-radius: 18px;
  padding: 20px;
  border: 1px solid rgba(37, 99, 235, 0.06);
}

.meta-section__header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.meta-section__label {
  font-weight: 700;
  font-size: 14px;
  color: #12213d;
}

.meta-section__body {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-tag {
  margin-bottom: 4px;
}

.meta-empty {
  color: #94a3b8;
  font-size: 13px;
}

.tag-checkbox {
  margin-right: 6px;
  margin-bottom: 6px;
}

.meta-row {
  width: 100%;
  display: flex;
  gap: 12px;
  padding: 6px 0;
  font-size: 13px;
}

.meta-row__key {
  color: #64748b;
  min-width: 70px;
}

.meta-row__value {
  color: #172033;
  font-weight: 500;
}
</style>
