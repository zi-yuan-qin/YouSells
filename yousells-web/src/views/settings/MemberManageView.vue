<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import PageSection from "@/components/app/PageSection.vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { RouteName } from "@/router/route-names";
import { fetchUserList, createUser, updateUser, resignUser } from "@/api/user";
import type { UserListItem, CreateUserRequest, UpdateUserRequest, ResignUserRequest } from "@/types/user";

const authStore = useAuthStore();
const router = useRouter();

const isT3 = computed(() => authStore.currentUser?.level === "T3");

const users = ref<UserListItem[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref<"create" | "edit">("create");
const editingUserId = ref<number | null>(null);

const resignDialogVisible = ref(false);
const resigningUser = ref<UserListItem | null>(null);
const resignForm = reactive<ResignUserRequest>({ reason: "" });
const resignFormRef = ref<FormInstance>();

const formRef = ref<FormInstance>();
const form = reactive<CreateUserRequest & { confirmPassword?: string }>({
  username: "",
  realName: "",
  password: "",
  level: "T0",
  managerUserId: null,
  confirmPassword: ""
});

const levelOptions = [
  { label: "T0 新人", value: "T0" },
  { label: "T1 初级", value: "T1" },
  { label: "T2 资深", value: "T2" },
  { label: "T3 主管", value: "T3" }
];

const managerNameMap = computed(() => {
  const map: Record<number, string> = {};
  for (const u of users.value) {
    map[u.userId] = u.realName;
  }
  return map;
});

const managerOptions = computed(() => {
  return users.value
    .filter(u => u.status === "ACTIVE")
    .map(u => ({ label: `${u.realName} (${u.level})`, value: u.userId }));
});

const formRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 50, message: "用户名 3-50 位", trigger: "blur" }
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (dialogMode.value === "edit") { callback(); return; }
        if (!value) { callback(new Error("请输入密码")); return; }
        if (value.length < 6 || value.length > 50) { callback(new Error("密码 6-50 位")); return; }
        callback();
      },
      trigger: "blur"
    }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (dialogMode.value === "edit") {
          callback();
          return;
        }
        if (!value) {
          callback(new Error("请确认密码"));
          return;
        }
        if (value !== form.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  level: [{ required: true, message: "请选择职级", trigger: "change" }]
};

async function loadUsers() {
  loading.value = true;
  try {
    users.value = await fetchUserList();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "加载成员列表失败");
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  dialogMode.value = "create";
  editingUserId.value = null;
  form.username = "";
  form.realName = "";
  form.password = "";
  form.level = "T0";
  form.managerUserId = null;
  form.confirmPassword = "";
  dialogVisible.value = true;
}

function openEditDialog(user: UserListItem) {
  dialogMode.value = "edit";
  editingUserId.value = user.userId;
  form.username = user.username;
  form.realName = user.realName;
  form.password = "";
  form.level = user.level;
  form.managerUserId = user.managerUserId;
  form.confirmPassword = "";
  dialogVisible.value = true;
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  try {
    if (dialogMode.value === "create") {
      const payload: CreateUserRequest = {
        username: form.username,
        realName: form.realName,
        password: form.password,
        level: form.level,
        managerUserId: form.managerUserId
      };
      await createUser(payload);
      ElMessage.success("成员创建成功");
    } else if (editingUserId.value !== null) {
      const payload: UpdateUserRequest = {
        realName: form.realName,
        level: form.level,
        managerUserId: form.managerUserId
      };
      await updateUser(editingUserId.value, payload);
      ElMessage.success("成员信息已更新");
    }
    dialogVisible.value = false;
    await loadUsers();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
  }
}

function openResignDialog(user: UserListItem) {
  resigningUser.value = user;
  resignForm.reason = "";
  resignDialogVisible.value = true;
}

async function handleResignSubmit() {
  const valid = await resignFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  if (!resigningUser.value) return;

  try {
    await resignUser(resigningUser.value.userId, { reason: resignForm.reason });
    ElMessage.success("已办理离职");
    resignDialogVisible.value = false;
    await loadUsers();
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "离职操作失败");
  }
}

function getLevelTagType(level: string) {
  const map: Record<string, "success" | "warning" | "info" | "danger"> = {
    T0: "info",
    T1: "success",
    T2: "warning",
    T3: "danger"
  };
  return map[level] ?? "info";
}

onMounted(() => {
  if (!isT3.value) {
    ElMessage.warning("无权访问成员管理");
    void router.replace({ name: RouteName.Dashboard });
    return;
  }
  void loadUsers();
});
</script>

<template>
  <div class="page-shell">
    <PageSection title="成员管理" description="管理团队成员账号、职级和状态">
      <template #extra>
        <el-button type="primary" @click="openCreateDialog">+ 新建成员</el-button>
        <el-button :loading="loading" @click="loadUsers">刷新</el-button>
      </template>

      <el-table v-loading="loading" :data="users" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="level" label="职级" min-width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getLevelTagType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerUserId" label="直属上级" min-width="120">
          <template #default="{ row }">
            <span v-if="row.managerUserId">
              {{ managerNameMap[row.managerUserId] ?? "-" }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === "ACTIVE" ? "正常" : "已禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="openResignDialog(row)">
              离职
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </PageSection>

    <el-dialog
      v-model="resignDialogVisible"
      title="办理离职"
      width="480px"
      destroy-on-close
    >
      <p v-if="resigningUser" class="mb-4">
        确认办理 <strong>{{ resigningUser.realName }}</strong>（{{ resigningUser.username }}）的离职手续？
      </p>
      <el-form ref="resignFormRef" :model="resignForm" label-width="80px">
        <el-form-item
          label="离职原因"
          prop="reason"
          :rules="[{ required: true, message: '请填写离职原因', trigger: 'blur' }]"
        >
          <el-input
            v-model="resignForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请填写离职原因（如：个人发展、合同到期等）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resignDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleResignSubmit">确认离职</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建成员' : '编辑成员'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="dialogMode === 'edit'" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="初始密码" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入" />
        </el-form-item>
        <el-form-item label="职级" prop="level">
          <el-select v-model="form.level" placeholder="选择职级" style="width: 100%">
            <el-option
              v-for="opt in levelOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="直属上级">
          <el-select
            v-model="form.managerUserId"
            clearable
            placeholder="选择直属上级（可选）"
            style="width: 100%"
          >
            <el-option
              v-for="opt in managerOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ dialogMode === "create" ? "创建" : "保存" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.text-muted {
  color: var(--color-text-muted);
}
</style>
