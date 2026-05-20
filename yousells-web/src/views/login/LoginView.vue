<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { RouteName } from "@/router/route-names";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const formRef = ref<FormInstance>();

const form = reactive({
  username: "admin",
  password: "admin123"
});

const rules: FormRules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  try {
    await authStore.loginAction(form);
    ElMessage.success("登录成功，已进入 YouSells 工作台");
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : undefined;
    await router.replace(redirect || { name: RouteName.Dashboard });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "登录失败");
  }
}
</script>

<template>
  <div class="login-shell">
    <div class="login-panel">
      <section class="login-panel__brand">
        <h1 class="login-panel__title">YouSells</h1>
        <p class="login-panel__desc">
          面向团队内部的客户管理与协作平台。P0 阶段先打通登录、看板、客户、跟进、公共安排、日报周报和话术库。
        </p>
        <ul class="login-panel__list">
          <li>统一客户口径与跟进节奏</li>
          <li>沉淀公共安排、日报周报和话术内容</li>
          <li>为 5 人左右团队建立正式开发与协作基线</li>
        </ul>
      </section>

      <section class="login-panel__form">
        <div class="login-form__header">
          <h1>进入项目工作台</h1>
          <p>当前提供内置演示账号，方便先打通前后端主流程，后续再接正式用户表与权限表。</p>
        </div>

        <el-form ref="formRef" class="login-form" label-position="top" :model="form" :rules="rules" @submit.prevent="handleSubmit">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="authStore.loading" :disabled="authStore.loading" @click="handleSubmit">
            登录 YouSells
          </el-button>
          <div class="login-tips">
            演示账号：`admin / admin123`，`member / member123`
          </div>
        </el-form>
      </section>
    </div>
  </div>
</template>
