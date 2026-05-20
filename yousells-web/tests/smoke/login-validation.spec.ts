import { test, expect } from "playwright/test";

const BASE_URL = "http://localhost:5173";

test.describe("FE-002 登录页", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState("networkidle");
  });

  test("页面正常渲染", async ({ page }) => {
    // 标题
    await expect(page.locator(".login-panel__title")).toHaveText("YouSells");
    // 表单
    await expect(page.locator(".login-form")).toBeVisible();
    // 账号密码输入框有默认值
    await expect(page.locator("input[placeholder='请输入账号']")).toHaveValue("admin");
    await expect(page.locator("input[placeholder='请输入密码']")).toHaveValue("admin123");
    // 登录按钮
    await expect(page.getByRole("button", { name: "登录 YouSells" })).toBeVisible();
  });

  test("账号为空时点登录标红提示", async ({ page }) => {
    // 清空账号
    await page.locator("input[placeholder='请输入账号']").fill("");
    // 点击登录
    await page.getByRole("button", { name: "登录 YouSells" }).click();
    // 应该出现校验错误
    await expect(page.locator(".el-form-item__error")).toBeVisible();
  });

  test("密码为空时点登录标红提示", async ({ page }) => {
    // 清空密码
    await page.locator("input[placeholder='请输入密码']").fill("");
    // 点击登录
    await page.getByRole("button", { name: "登录 YouSells" }).click();
    // 应该出现校验错误
    await expect(page.locator(".el-form-item__error")).toBeVisible();
  });

  test("账号密码正确填写可以尝试登录（按钮 loading）", async ({ page }) => {
    await page.locator("input[placeholder='请输入账号']").fill("admin");
    await page.locator("input[placeholder='请输入密码']").fill("admin123");
    // 点击登录
    await page.getByRole("button", { name: "登录 YouSells" }).click();
    // 按钮应该进入 loading 状态（说明请求发出去了）
    await expect(page.getByRole("button", { name: "登录 YouSells" })).toBeDisabled();
  });
});
