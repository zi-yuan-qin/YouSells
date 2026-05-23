<script setup lang="ts">
import { ref, reactive, watch, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import type { CustomerDetail } from "@/types/customer-detail";
import { updateCustomer } from "@/api/customer-detail";

const props = defineProps<{
  detail: CustomerDetail;
  loading: boolean;
}>();

const emit = defineEmits<{
  updated: [];
}>();

const userStore = useUserStore();
const editing = ref(false);
const saving = ref(false);

function avatarChar(name: string) {
  return name ? name.charAt(0) : "?";
}

function avatarColor(name: string) {
  if (!name) return "#94a3b8";
  const colors = ["#2563eb", "#7c3aed", "#db2777", "#ea580c", "#059669", "#0891b2", "#4f46e5", "#be123c"];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return colors[Math.abs(hash) % colors.length];
}

const gradeOptions = ["大一", "大二", "大三", "大四"];
const progressOptions = ["职规", "技术栈", "课程"];
const intentOptions = ["很稳", "可跟", "观望", "冷淡"];

const editForm = reactive({
  realName: "",
  grade: "",
  major: "",
  className: "" as string | null,
  progress: "",
  intent: "",
  ownerUserId: 0,
  inviterUserId: 0,
  inviterNote: "" as string | null
});

function startEdit() {
  editForm.realName = props.detail.realName;
  editForm.grade = props.detail.grade;
  editForm.major = props.detail.major;
  editForm.className = props.detail.className;
  editForm.progress = props.detail.progress;
  editForm.intent = props.detail.intent;
  editForm.ownerUserId = props.detail.ownerUserId;
  editForm.inviterUserId = props.detail.inviterUserId;
  editForm.inviterNote = props.detail.inviterNote;
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
}

async function saveEdit() {
  if (!editForm.realName || !editForm.grade || !editForm.major || !editForm.progress || !editForm.intent) {
    ElMessage.warning("请填写必填项");
    return;
  }
  saving.value = true;
  try {
    await updateCustomer(props.detail.id, {
      realName: editForm.realName,
      grade: editForm.grade,
      major: editForm.major,
      className: editForm.className,
      progress: editForm.progress,
      intent: editForm.intent,
      ownerUserId: editForm.ownerUserId,
      inviterUserId: editForm.inviterUserId,
      inviterNote: editForm.inviterNote
    });
    ElMessage.success("客户信息已更新");
    editing.value = false;
    emit("updated");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "更新失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void userStore.loadUsers();
});
</script>

<template>
  <div class="profile-card">
    <div class="profile-card__identity" v-if="!editing">
      <div
        class="profile-card__avatar"
        :style="{ background: avatarColor(detail.realName) }"
      >
        {{ avatarChar(detail.realName) }}
      </div>
      <div class="profile-card__name-block">
        <div class="profile-card__name">{{ detail.realName }}</div>
        <div class="profile-card__sub">{{ detail.grade }} · {{ detail.major }}</div>
      </div>
    </div>

    <div class="profile-card__header">
      <h3 class="page-section__title" style="font-size: 18px; margin: 0">
        {{ editing ? '编辑档案' : '详细信息' }}
      </h3>
      <el-button
        v-if="!editing"
        type="primary"
        size="small"
        text
        @click="startEdit"
      >
        编辑
      </el-button>
      <div v-else style="display: flex; gap: 8px;">
        <el-button size="small" @click="cancelEdit">取消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="saveEdit">
          保存
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="detail-grid">
      <template v-if="!editing">
        <div class="detail-item">
          <div class="detail-item__label">姓名</div>
          <div class="detail-item__value">{{ detail.realName }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">年级</div>
          <div class="detail-item__value">{{ detail.grade }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">专业</div>
          <div class="detail-item__value">{{ detail.major }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">班级</div>
          <div class="detail-item__value">{{ detail.className || "暂无" }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">当前进度</div>
          <div class="detail-item__value">{{ detail.progress }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">意向</div>
          <div class="detail-item__value">{{ detail.intent }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">负责人</div>
          <div class="detail-item__value">{{ detail.ownerDisplayName }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">邀约人</div>
          <div class="detail-item__value">{{ detail.inviterDisplayName || "暂无" }}</div>
        </div>
      </template>

      <template v-else>
        <div class="detail-item">
          <div class="detail-item__label">姓名</div>
          <el-input v-model="editForm.realName" placeholder="请输入姓名" size="small" />
        </div>
        <div class="detail-item">
          <div class="detail-item__label">年级</div>
          <el-select v-model="editForm.grade" placeholder="请选择" size="small" style="width: 100%">
            <el-option v-for="opt in gradeOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">专业</div>
          <el-input v-model="editForm.major" placeholder="请输入专业" size="small" />
        </div>
        <div class="detail-item">
          <div class="detail-item__label">班级</div>
          <el-input v-model="editForm.className" placeholder="请输入班级" size="small" />
        </div>
        <div class="detail-item">
          <div class="detail-item__label">当前进度</div>
          <el-select v-model="editForm.progress" placeholder="请选择" size="small" style="width: 100%">
            <el-option v-for="opt in progressOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">意向</div>
          <el-select v-model="editForm.intent" placeholder="请选择" size="small" style="width: 100%">
            <el-option v-for="opt in intentOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">负责人</div>
          <el-select v-model="editForm.ownerUserId" placeholder="请选择" size="small" style="width: 100%">
            <el-option
              v-for="opt in userStore.userOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
        <div class="detail-item">
          <div class="detail-item__label">邀约人</div>
          <el-select v-model="editForm.inviterUserId" placeholder="请选择" size="small" style="width: 100%">
            <el-option
              v-for="opt in userStore.userOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.profile-card {
  background: var(--color-bg-card);
  border-radius: 18px;
  padding: 20px;
  border: 1px solid var(--color-border);
}

.profile-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.profile-card__identity {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.profile-card__avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.profile-card__name-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.profile-card__name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.profile-card__sub {
  font-size: 13px;
  color: var(--color-text-muted);
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 20px;
}

.detail-item__label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.detail-item__value {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}
</style>
