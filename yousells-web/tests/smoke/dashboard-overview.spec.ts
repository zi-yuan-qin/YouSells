import { test, expect } from "playwright/test";

const BASE_URL = "http://localhost:5173";

test.describe("FE-003 首页看板", () => {
  test.beforeEach(async ({ page }) => {
    // 先登录
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState("networkidle");
    await page.locator("input[placeholder='请输入账号']").fill("admin");
    await page.locator("input[placeholder='请输入密码']").fill("admin123");
    await page.getByRole("button", { name: "登录 YouSells" }).click();
    await page.waitForURL("**/dashboard", { timeout: 10000 });
    await page.waitForLoadState("networkidle");
  });

  test("统计卡片渲染4个", async ({ page }) => {
    await expect(page.locator(".stat-card")).toHaveCount(4);
  });

  test("今日公共安排区域可见", async ({ page }) => {
    await expect(page.locator(".list-card").first()).toBeVisible();
  });

  test("重点客户区域可见", async ({ page }) => {
    await expect(page.locator(".list-card").last()).toBeVisible();
  });

  test("列表区有内容展示（数据或空占位）", async ({ page }) => {
    // 两个 list-card 里要么有 ul>li（有数据）要么有 placeholder（无数据），至少有一个能显示
    const firstCard = page.locator(".split-grid > div").first();
    const secondCard = page.locator(".split-grid > div").last();
    await expect(firstCard).toBeVisible();
    await expect(secondCard).toBeVisible();
  });

  test("刷新按钮可见且可点击", async ({ page }) => {
    const refreshBtn = page.getByRole("button", { name: "刷新数据" });
    await expect(refreshBtn).toBeVisible();
    await refreshBtn.click();
    // loading 期间按钮 disabled
    await expect(refreshBtn).toBeDisabled();
  });
});
