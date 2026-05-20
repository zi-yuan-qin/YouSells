import { test, expect } from "playwright/test";

const BASE_URL = "http://localhost:5173";

test.describe("FE-004 客户列表", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState("networkidle");
    await page.locator("input[placeholder='请输入账号']").fill("admin");
    await page.locator("input[placeholder='请输入密码']").fill("admin123");
    await page.getByRole("button", { name: "登录 YouSells" }).click();
    await page.waitForURL("**/dashboard", { timeout: 10000 });
    await page.click("a[href='/customers']");
    await page.waitForURL("**/customers", { timeout: 10000 });
    await page.waitForLoadState("networkidle");
  });

  test("筛选栏渲染正常", async ({ page }) => {
    await expect(page.locator(".filter-bar")).toBeVisible();
    await expect(page.getByRole("button", { name: "搜索" })).toBeVisible();
    await expect(page.getByRole("button", { name: "重置" })).toBeVisible();
  });

  test("表格渲染有数据", async ({ page }) => {
    await expect(page.locator(".el-table")).toBeVisible();
    await page.waitForSelector(".el-table__body tr", { timeout: 10000 });
    const rows = await page.locator(".el-table__body tr").count();
    expect(rows).toBeGreaterThan(0);
  });

  test("标签面板渲染", async ({ page }) => {
    await expect(page.locator(".tag-panel")).toBeVisible();
  });

  test("点击行跳转客户详情", async ({ page }) => {
    await page.locator(".el-table__body tr").first().click();
    await expect(page).toHaveURL(/\/customers\/\d+/);
  });

  test("重置按钮清空筛选", async ({ page }) => {
    const keywordInput = page.locator(".filter-bar input").first();
    await keywordInput.fill("测试");
    await page.getByRole("button", { name: "重置" }).click();
    await expect(keywordInput).toHaveValue("");
  });
});
