import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus, { ElMessage } from "element-plus";
// @ts-ignore
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import { RouteName } from "@/router/route-names";
import { useAuthStore } from "@/stores/auth";
import { AUTH_UNAUTHORIZED_EVENT } from "@/utils/auth-token";
import "./styles/base.css";
import "./styles/animations.css";

// Initialize theme before rendering
const savedTheme = localStorage.getItem("yousells-theme");
const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
const initialTheme = savedTheme === "dark" || savedTheme === "light" ? savedTheme : prefersDark ? "dark" : "light";
document.documentElement.setAttribute("data-theme", initialTheme);

function installMessageGuard() {
  const originalError = ElMessage.error;

  ElMessage.error = ((options: Parameters<typeof originalError>[0], appContext?: Parameters<typeof originalError>[1]) => {
    const message =
      typeof options === "string"
        ? options
        : typeof options === "object" && options !== null && "message" in options
          ? String((options as { message?: unknown }).message ?? "")
          : "";
    if (message === "login required") {
      return {} as ReturnType<typeof originalError>;
    }
    return originalError(options as never, appContext as never);
  }) as typeof ElMessage.error;
}

const app = createApp(App);
const pinia = createPinia();

installMessageGuard();

app.use(pinia).use(router).use(ElementPlus, { locale: zhCn });

const authStore = useAuthStore(pinia);

window.addEventListener(AUTH_UNAUTHORIZED_EVENT, async (event: Event) => {
  authStore.clearAuthStateAction();

  const redirect =
    event instanceof CustomEvent && typeof event.detail?.redirect === "string"
      ? event.detail.redirect
      : router.currentRoute.value.fullPath;

  if (router.currentRoute.value.name !== RouteName.Login) {
    await router.replace({
      name: RouteName.Login,
      query: redirect ? { redirect } : {}
    });
  }
});

app.mount("#app");
