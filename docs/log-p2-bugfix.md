# P2 Bug 修复日志

## 状态：Phase 3 完成，等待 Phase 5 用户联调验收

## 变更记录

| 时间 | 批次 | 文件 | 变更 | 原因 |
|---|---|---|---|---|
| 2026-05-25 00:05 | — | CustomerDetailView.vue | 删除"写跟进"按钮 | 无效交互，已推送到 main |
| 2026-05-25 00:10 | — | plan-p2-bugfix.md, log-p2-bugfix.md | 创建规划文档和日志 | Phase 2 |
| 2026-05-25 00:20 | A1 | TopicFilterBar.vue | 补充 Search 图标 import | 模板使用未导入的图标，运行时崩溃 |
| 2026-05-25 00:20 | A2 | NotificationBell.vue | onMounted 设置 isMounted=true；导航改用 RouteName | WebSocket 断线后永不重连；硬编码路径 |
| 2026-05-25 00:20 | A3 | CustomerDetailView.vue | Promise.all→Promise.allSettled 独立容错；requestId 防竞态；删除死 import；路径改 RouteName | 子资源失败导致整页数据丢失；快速切换客户数据错乱 |
| 2026-05-25 00:20 | A4 | TaskBoardView.vue | 添加 loadError 状态区分"加载失败"和"空数据" | API 失败时显示"空空如也"误导用户 |
| 2026-05-25 00:20 | A5 | TopicListView.vue | "发起提问"按钮改为打开 TopicCreateDialog | 导航到不存在的 topic-create 路由 |
| 2026-05-25 00:25 | B1 | CustomerListView.vue | :loading 绑定 reactive ref；修复 onQueryUpdate | 筛选栏永久 disabled；reactive 对象清空丢失属性 |
| 2026-05-25 00:25 | B2 | ReportPlazaView.vue | skipFilterWatch 标志消除切换 Tab 时双重请求 | 每次切 Tab 发起两次相同 API 请求 |
| 2026-05-25 00:25 | B3 | NotificationListView.vue | 添加错误处理、await markRead、@size-change 处理 | 无错误处理、fire-and-forget、切 pageSize 无效 |
| 2026-05-25 00:25 | B4 | TaskBoardView.vue + api/task.ts + types/task.ts | 新增 updateTaskStatus API 和 TaskStatusUpdateRequest 类型；handleStatusChange 只发送 status | 全量覆盖导致并发编辑丢数据 |
| 2026-05-25 00:30 | C1/C2 | CustomerMetaPanel.vue, CustomerNextActionCard.vue, CustomerDetailView.vue | 删除从未触发的 emit 声明和父组件监听器 | tags-updated/updated emit 定义了但从未触发 |
| 2026-05-25 00:30 | C3/C4 | DailyReportView.vue, WeeklyReportView.vue | loadHistory 接受 pageSize 参数 | 切换 pageSize 无效 |
| 2026-05-25 00:30 | C5/C6 | api/leaderboard.ts, api/notification.ts, NotificationListView.vue, NotificationBell.vue, LeaderboardView.vue | API 返回值统一 unwrap，同步更新所有调用方 | 与项目其他 API 模块不一致 |
| 2026-05-25 00:30 | C7 | CustomerTable.vue | 删除未使用的 Edit, Phone import | 死代码 |
| 2026-05-25 00:30 | C8 | DashboardView.vue | 沉默客户 CSS 互斥；图表主题 MutationObserver；简化 stat click | CSS 类重叠；暗色模式下图表不更新 |
| 2026-05-25 00:30 | C9 | LeaderboardView.vue | 添加 try-catch 错误处理 | 加载失败白屏无提示 |
| 2026-05-25 00:30 | C10 | ScriptLibraryView.vue | 分类加载失败添加错误提示；预览 drawer loading 独立 | 静默吞错；预览时绑定表格 loading |
| 2026-05-25 00:30 | C11 | CustomerDetailView.vue + api/followup.ts | 进度百分比改用配置映射表；fetchFollowUps 支持分页参数；添加跟进分页器 | 硬编码映射；跟进超过20条看不到旧的 |
| 2026-05-25 00:35 | D1 | ProfileView.vue | 删除 onMounted 中冗余赋值 | loadProfile 立即覆盖 |
| 2026-05-25 00:35 | D2 | MemberManageView.vue | 密码校验区分创建/编辑模式；O(n) computed map 替代 O(n²) 模板查找 | 编辑模式误校验；性能优化 |
| 2026-05-25 00:35 | D3 | TopicDetailView.vue | 404 检测改用 Axios response.status | 字符串匹配 404 不可靠 |
| 2026-05-25 00:35 | D4 | ReportCommentPanel.vue | 错误提示包含 API 返回信息 | 与其他组件不一致 |
| 2026-05-25 00:40 | 全部 | 全量 | `vue-tsc --noEmit` 通过 + `npm run build` 通过 | 验证阶段 |

## 涉及文件统计

- 视图文件：15/15
- 组件文件：8 个
- API 文件：4 个
- 类型文件：1 个
- 总计：28 个文件

## 验收状态

- [x] 批次 A（6 条 Critical）
- [x] 批次 B（5 条 High）
- [x] 批次 C（11 条 Medium）
- [x] 批次 D（4 条 Low）
- [x] `npm run build` 通过
- [ ] 用户联调验收
