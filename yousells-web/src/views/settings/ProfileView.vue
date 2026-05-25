<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import { useAuthStore } from "@/stores/auth";
import { fetchUserProfile, updateUserProfile, updateUserPassword } from "@/api/user";
import type { UpdatePasswordRequest, UpdateProfileRequest } from "@/types/user";

const authStore = useAuthStore();

const profileLoading = ref(false);
const passwordLoading = ref(false);

const profileForm = reactive<UpdateProfileRequest>({
  realName: ""
});

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();

const levelLabel = computed(() => {
  const level = authStore.currentUser?.level ?? "";
  const map: Record<string, string> = {
    T0: "T0 新人",
    T1: "T1 初级",
    T2: "T2 资深",
    T3: "T3 主管"
  };
  return map[level] ?? level;
});

const profileRules: FormRules = {
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }]
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};

async function loadProfile() {
  try {
    const profile = await fetchUserProfile();
    profileForm.realName = profile.realName;
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "加载个人信息失败");
  }
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  profileLoading.value = true;
  try {
    await updateUserProfile({ realName: profileForm.realName });
    ElMessage.success("个人信息已更新");
    await authStore.fetchCurrentUserAction();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "更新失败");
  } finally {
    profileLoading.value = false;
  }
}

async function handleUpdatePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  passwordLoading.value = true;
  try {
    const payload: UpdatePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    };
    await updateUserPassword(payload);
    ElMessage.success("密码已修改，请使用新密码重新登录");
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "修改密码失败");
  } finally {
    passwordLoading.value = false;
  }
}

onMounted(() => {
  void loadProfile();
});
</script>

<template>
  <div class="page-shell">
    <PageSection title="个人设置" description="管理你的个人信息和账号安全">
      <div class="profile-grid">
        <div class="profile-card">
          <h3 class="profile-card__title">基本信息</h3>
          <div class="profile-readonly">
            <div class="profile-field">
              <span class="profile-field__label">用户名</span>
              <span class="profile-field__value">{{ authStore.currentUser?.username }}</span>
            </div>
            <div class="profile-field">
              <span class="profile-field__label">职级</span>
              <el-tag size="small" type="primary">{{ levelLabel }}</el-tag>
            </div>
          </div>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-position="top"
            style="margin-top: 16px"
          >
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-button
              type="primary"
              :loading="profileLoading"
              @click="handleUpdateProfile"
            >
              保存修改
            </el-button>
          </el-form>
        </div>

        <div class="profile-card">
          <h3 class="profile-card__title">修改密码</h3>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="至少 6 位"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="再次输入新密码"
              />
            </el-form-item>
            <el-button
              type="primary"
              :loading="passwordLoading"
              @click="handleUpdatePassword"
            >
              修改密码
            </el-button>
          </el-form>
        </div>
      </div>
    </PageSection>
  </div>
</template>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-card);
}

.profile-card__title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.profile-readonly {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.profile-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-field__label {
  font-size: 12px;
  color: var(--color-text-muted);
  min-width: 60px;
}

.profile-field__value {
  font-weight: 600;
  color: var(--color-text-primary);
}
</style>
