# P2 Bug 修复规划

## 模块概述

修复前端 UI 审查发现的全部 26 条 bug，覆盖 15 个页面和 6 个组件/API 模块。

## 文件清单

### 批次 A：Critical（6 条，4 个文件）

| # | 文件 | 修改内容 | 风险 |
|---|---|---|---|
| A1 | `yousells-web/src/components/topic/TopicFilterBar.vue` | 补充 `Search` 图标 import | 低 |
| A2 | `yousells-web/src/components/app/NotificationBell.vue` | `onMounted` 中设置 `isMounted = true`；硬编码路径改为 RouteName | 低 |
| A3 | `yousells-web/src/views/customer/CustomerDetailView.vue` | Promise.all 改为独立 try-catch；添加 AbortController 防竞态；删除未使用的 import；硬编码路径改 RouteName | 中 |
| A4 | `yousells-web/src/views/task/TaskBoardView.vue` | 区分"加载失败"与"空数据"两种空状态 | 低 |
| A5 | `yousells-web/src/views/topic/TopicListView.vue` | "发起提问"按钮改为打开 TopicCreateDialog | 低 |

### 批次 B：High（5 条，4 个文件）

| # | 文件 | 修改内容 | 风险 |
|---|---|---|---|
| B1 | `yousells-web/src/views/customer/CustomerListView.vue` | `:loading` 改为 `:loading="loading"`；修复 `onQueryUpdate` 中 reactive 对象清空方式 | 中 |
| B2 | `yousells-web/src/views/report/ReportPlazaView.vue` | Tab 切换 watch 中去掉冗余的 `filterUserId = null`，消除重复请求 | 低 |
| B3 | `yousells-web/src/views/notification/NotificationListView.vue` | 添加 try-catch 错误处理；乐观更新添加 rollback；handleClickItem 中 await markRead；添加 @size-change 处理 | 中 |
| B4 | `yousells-web/src/views/task/TaskBoardView.vue` | handleStatusChange 改为只发送变化的字段 | 低 |

### 批次 C：Medium（11 条，9 个文件）

| # | 文件 | 修改内容 | 风险 |
|---|---|---|---|
| C1 | `yousells-web/src/components/customer-detail/CustomerMetaPanel.vue` | 确认 tags 编辑逻辑是否需要 emit，如无需则删除 emit 声明 | 低 |
| C2 | `yousells-web/src/components/customer-detail/CustomerNextActionCard.vue` | 同上，确认并修复或删除 `updated` emit | 低 |
| C3 | `yousells-web/src/views/report/DailyReportView.vue` | loadHistory 接受 pageSize 参数；添加持久错误状态 | 低 |
| C4 | `yousells-web/src/views/report/WeeklyReportView.vue` | 同上 | 低 |
| C5 | `yousells-web/src/api/leaderboard.ts` | 统一 API 返回值结构（unwrap data） | 中 |
| C6 | `yousells-web/src/api/notification.ts` | 同上；同步修改所有调用方 | 中 |
| C7 | `yousells-web/src/components/customer-list/CustomerTable.vue` | 删除未使用的 Edit、Phone import | 低 |
| C8 | `yousells-web/src/views/dashboard/DashboardView.vue` | 3 个统计卡片补全导航过滤参数；修复 CSS 类重叠；图表主题响应式 | 中 |
| C9 | `yousells-web/src/views/leaderboard/LeaderboardView.vue` | 添加 try-catch 错误处理 | 低 |
| C10 | `yousells-web/src/views/script/ScriptLibraryView.vue` | 分类加载失败添加错误提示；预览 drawer loading 改用独立 ref | 低 |
| C11 | `yousells-web/src/views/customer/CustomerDetailView.vue` | 进度百分比改为常量映射表；跟进记录添加分页 | 中 |

### 批次 D：Low（4 条，4 个文件）

| # | 文件 | 修改内容 | 风险 |
|---|---|---|---|
| D1 | `yousells-web/src/views/settings/ProfileView.vue` | 删除 onMounted 中多余的赋值 | 低 |
| D2 | `yousells-web/src/views/settings/MemberManageView.vue` | 密码字段编辑模式下跳过 required 校验；O(n^2) 查找改为 computed map | 低 |
| D3 | `yousells-web/src/views/topic/TopicDetailView.vue` | 404 检测改用 Axios 状态码判断 | 低 |
| D4 | `yousells-web/src/components/report/ReportCommentPanel.vue` | 错误提示包含 API 返回信息 | 低 |

## 接口定义

本批次为 bug 修复，不新增 API，不修改数据结构。仅涉及前端内部逻辑修正。

## 假设列表

1. 后端 API 行为不变，所有 bug 仅源于前端代码问题
2. Dashboard 统计卡片导航到 CustomerList 后，CustomerList 支持通过 query 参数过滤（需确认）
3. API 返回值结构统一（leaderboard/notification unwrap）不影响后端

## 风险点

- **A3** CustomerDetailView 改动较大（Promise.all → 独立调用），需确保 loading 状态逻辑正确
- **B3** NotificationListView 乐观更新回滚逻辑需仔细处理
- **C5/C6** API 返回值结构统一会影响所有调用方，需同步修改 LeaderboardView、NotificationListView、NotificationBell
- **C8** Dashboard 图表主题响应式需引入 theme watch，注意内存泄漏

## 执行顺序

按 A → B → C → D 批次顺序执行，每批次完成后运行 `npm run build` 确认无编译错误。
