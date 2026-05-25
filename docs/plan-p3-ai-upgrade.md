# YouSells P3 智能化升级 — 总任务分配与进度管理

日期：`2026-05-25`
当前阶段：P0/P1/P2 完成 → **P3 AI 智能化升级启动**

---

## 1. 项目定位

| 项 | 内容 |
|---|---|
| 项目名称 | YouSells — 客户管理与团队协作平台 |
| 当前阶段 | P3: 智能化升级 |
| 目标版本 | v1.3.0 |
| 目标用户 | 内部 5 人团队（50-100 潜在用户） |
| 成功标准 | 4 个 AI 模块独立可交付，每人完成一个端到端功能模块 |

---

## 2. 当前状态扫描

| 层 | 当前状态 | 缺口 | 确认人 |
|---|---|---|---|
| 数据库 | MySQL 8.4 + Qdrant（向量数据库，Docker 部署），`task_boards`/`customer_follow_ups`/`script_library`/`reports` 等表就绪 | 需新增 AI 结果缓存表 | 梓源 |
| 后端 | Spring Boot 3.5，9 个业务模块，186 测试全过 | 需新增 LLM 调用基础设施(Prompt 管理/JSON 解析/缓存) + Qdrant SDK 封装 | 梓源 |
| 前端 | Vue 3 + Element Plus，16 个页面，P2 bug 已清零 | 需新增 AI 结果展示组件(洞察面板/推荐侧栏/风险卡片) | 梓源 |
| 测试 | 186 后端测试，前端 Playwright 已配置 | 每个 AI 模块需独立测试方案 | 梓源 |
| 文档 | docs/00-08，P0/P1/P2 规划+日志就绪 | 需新增 P3 总规划 + 4 份个人任务说明书 | 梓源 |

---

## 3. 范围锁定

### 在范围

| 模块 | 代号 | 一句话目标 | 独立性 | 建议负责人 |
|---|---|---|---|---|
| 客户 360° 智能洞察 | P3-AI-INSIGHT | AI 自动分析跟进历史 → 意向趋势/关键点/转化概率/下一步建议 | 独立于其他模块 | 志明 |
| AI 日报/周报自动生成 | P3-AI-REPORT | 根据当日活动数据 AI 预填日报摘要，周报自动汇总 | 独立于其他模块 | 哲涛 |
| 智能话术检索与推荐 | P3-AI-SCRIPT | 跟进时 AI 根据客户上下文推荐最匹配话术，支持语义搜索 | 独立于其他模块 | 许润 |
| 客户流失预警引擎 | P3-AI-CHURN | AI 自动识别流失风险客户，分级预警+干预建议 | 独立于其他模块 | 嘉诚 |

### 不在范围

| 项 | 原因 |
|---|---|
| LLM 模型选型与 API Key 管理 | 队长基础设施，Phase 0 前置 |
| AI 训练/微调 | 用 Prompt Engineering + Embedding，不涉及模型训练 |
| AI 训练/微调 | 用 Prompt Engineering，不涉及模型训练 |
| 实时 AI 对话/聊天机器人 | P4 考虑 |
| 移动端适配 | P4 考虑 |

---

## 4. 队长前置基础设施（LEAD-P3）

队长在成员并行开发前必须完成的基础设施：

| 任务编号 | 目标 | 预估 | 交付物 |
|---|---|---|---|
| LEAD-P3-001 | LLM 调用基础设施：`AiService` 统一封装（Prompt 模板管理 + JSON Schema 约束输出 + 超时/重试/降级 + Embedding 调用） | 2天 | `yousells-server/src/main/java/com/yousells/common/ai/` |
| LEAD-P3-002 | Qdrant 向量数据库：docker-compose 加服务 + `EmbeddingService` 封装（文本→向量→写入 Qdrant→相似度检索） | 0.5天 | docker-compose.yml + `common/ai/EmbeddingService.java` |
| LEAD-P3-003 | AI 配置管理：`application.yml` 中 LLM 参数（endpoint/apiKey/model/temperature）+ Qdrant 连接参数，dev 环境可用 mock | 0.5天 | 配置文件 |
| LEAD-P3-004 | 前端 AI 加载态组件：`AiLoadingCard.vue`（思考中动画/结果展示/错误重试） | 0.5天 | 1 个组件 |
| LEAD-P3-005 | P3 数据库迁移脚本：AI 结果缓存表 + 风险评分表 | 0.5天 | SQL 脚本 |
| LEAD-P3-006 | 4 份个人任务说明书 | 2天 | docs/P3-{志明/哲涛/许润/嘉诚}.md |

**队长基础设施不阻塞成员启动**：成员在拿到任务说明书后即可在自己的模块目录下编码，队长并行推进基础设施。

---

## 5. 团队分工总览

| 成员 | 角色 | 优势 | 模块 | 涉及域 | 最终交付 |
|---|---|---|---|---|---|
| 秦梓源 | 队长/架构 | 全栈+基础设施 | LEAD-P3 | 公共基础设施 | AiService + Qdrant + 6 份说明书 + 集成验收 |
| 志明 | 后端 | 客户域/跟进域 | P3-AI-INSIGHT | customer / followup | 客户洞察面板前后端 |
| 哲涛 | 后端 | 日报周报/任务/话术 | P3-AI-REPORT | report / task / followup / customer | AI 日报周报前后端 |
| 许润 | 前端 | Vue 组件/交互 | P3-AI-SCRIPT | script / followup / customer / Qdrant | 话术推荐侧栏前后端（含向量检索） |
| 嘉诚 | 前端 | Dashboard/UI | P3-AI-CHURN | customer / followup / dashboard / notification | 流失预警引擎前后端 |

---

## 6. 依赖图

```text
┌─────────────────────────────────────────────────────────┐
│              队长 LEAD-P3（第1周）                        │
│  AiService / Qdrant / 配置 / 前端组件 / DB脚本 / 说明书     │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────┐
        ▼            ▼            ▼            ▼
   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
   │ 志明     │ │ 哲涛     │ │ 许润     │ │ 嘉诚     │
   │ P3-AI-   │ │ P3-AI-   │ │ P3-AI-   │ │ P3-AI-   │
   │ INSIGHT  │ │ REPORT   │ │ SCRIPT   │ │ CHURN    │
   └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
        │            │            │            │
        └────────────┼────────────┼────────────┘
                     ▼
          ┌─────────────────────┐
          │  队长集成验收（第3周）  │
          │  联调 + Review + PR   │
          └─────────────────────┘
```

四个模块**零相互依赖**，完全并行。仅依赖队长基础设施（LEAD-P3-001 ~ 005）。许润模块额外依赖 Qdrant（LEAD-P3-002），拿到说明书后前端部分不受阻塞可先行启动。

---

## 7. 文件所有权（防 PR 冲突）

### 原则
- **每人独占自己的模块目录**，不修改他人文件
- **共享文件只读**：`common/` `router/` `stores/` `layouts/` 由队长独占
- **跨模块 API 调用**：通过已有 Service 接口，不直接改他模块内部实现

### 所有权表

| 路径 | 所有者 | 权限 |
|---|---|---|
| `yousells-server/src/main/java/com/yousells/common/ai/` | 梓源 | 独占 |
| `yousells-server/src/main/java/com/yousells/modules/customer/` | 志明 | 可写 |
| `yousells-server/src/main/java/com/yousells/modules/followup/` | 志明 | 可写 |
| `yousells-server/src/main/java/com/yousells/modules/report/` | 哲涛 | 可写 |
| `yousells-server/src/main/java/com/yousells/modules/script/` | 许润 | 可写 |
| `yousells-server/src/main/java/com/yousells/modules/task/` | 哲涛/嘉诚 | 只读 |
| `yousells-server/src/main/java/com/yousells/modules/dashboard/` | 嘉诚 | 可写 |
| `yousells-server/src/main/java/com/yousells/modules/notification/` | 嘉诚 | 可写 |
| `yousells-web/src/components/ai/` | 梓源 | 独占（公共 AI 组件） |
| `yousells-web/src/components/insight/` | 志明 | 独占 |
| `yousells-web/src/components/report/` | 哲涛 | 可写 |
| `yousells-web/src/components/script/` | 许润 | 可写 |
| `yousells-web/src/components/dashboard/` | 嘉诚 | 可写 |
| `yousells-web/src/views/customer/CustomerDetailView.vue` | 志明 | 可写（AI 洞察面板 slot） |
| `yousells-web/src/views/report/DailyReportView.vue` | 哲涛 | 可写 |
| `yousells-web/src/views/report/WeeklyReportView.vue` | 哲涛 | 可写 |
| `yousells-web/src/views/script/ScriptLibraryView.vue` | 许润 | 可写 |
| `yousells-web/src/views/dashboard/DashboardView.vue` | 嘉诚 | 可写 |
| `yousells-web/src/router/` | 梓源 | 独占（共享） |
| `yousells-web/src/stores/` | 梓源 | 独占（共享） |
| `yousells-web/src/layouts/` | 梓源 | 独占（共享） |
| `yousells-web/src/utils/request.ts` | 梓源 | 独占（共享） |
| `yousells-server/src/main/java/com/yousells/common/` | 梓源 | 独占（共享） |

---

## 8. 数据库变更

| 表 | 操作 | 说明 | 负责人 |
|---|---|---|---|
| `ai_insight_cache` | 新建（MySQL） | customer_id, insight_json, generated_at, expires_at | 志明 |
| `ai_churn_risk` | 新建（MySQL） | customer_id, risk_level, risk_score, risk_factors(json), suggestion, evaluated_at | 嘉诚 |
| `script_vectors` | 新建（Qdrant collection） | 话术标题+内容→embedding 向量，由队长 LEAD-P3-002 初始化 | 梓源 |

**迁移文件：** `yousells-server/src/main/resources/db/05_schema_p3.sql`
**Qdrant 初始化：** 队长在 `EmbeddingService` 中提供 `initScriptVectors()` 方法，话术库 CRUD 时同步更新向量

---

## 9. API 端点总览

| 模块 | 方法 | 路径 | 说明 | 负责人 |
|---|---|---|---|---|
| INSIGHT | GET | `/api/customers/{id}/ai-insight` | 获取客户 AI 洞察 | 志明 |
| INSIGHT | POST | `/api/customers/{id}/ai-insight/refresh` | 强制刷新洞察 | 志明 |
| REPORT | POST | `/api/reports/daily/ai-generate` | AI 生成今日日报摘要 | 哲涛 |
| REPORT | POST | `/api/reports/weekly/ai-generate` | AI 生成本周周报摘要 | 哲涛 |
| SCRIPT | POST | `/api/scripts/recommend` | 根据客户上下文推荐话术 | 许润 |
| SCRIPT | GET | `/api/scripts/search` | 语义搜索话术（新增 keyword → AI 增强搜索） | 许润 |
| CHURN | GET | `/api/dashboard/churn-risks` | 获取流失风险客户列表 | 嘉诚 |
| CHURN | POST | `/api/customers/churn/evaluate` | 触发流失风险评估 | 嘉诚 |

---

## 10. 进度管理

### 状态词汇

| 状态 | 含义 |
|---|---|
| 🔴 未开始 | 任务已分配，尚未启动 |
| 🟡 进行中 | 正在编码，日志同步更新 |
| 🟢 已完成 | 代码+测试+Review 通过 |
| ⚫ 阻塞 | 遇到无法独立解决的问题 |

### 阻塞定义与处理
- 阻塞条件：依赖的 API 不可用 / 技术方案不确定 / 需要队长决策
- 处理：阻塞后 **当天内** 在群内说明阻塞原因 + 尝试过的方案 → 队长 24h 内响应
- 不建议自己死磕超过半天

### 每日更新格式
```
【P3-xxx 进度】
- 今日完成：xxx
- 遇到的问题：xxx / 无
- 明天计划：xxx
- 需要队长协助：xxx / 无
```

### 完成定义
- [ ] 代码通过编译（前端 `vue-tsc --noEmit` + `npm run build`，后端 `mvn compile`）
- [ ] 测试全部通过（前端 Playwright / 后端 `mvn test`）
- [ ] 验收清单逐项通过（对照个人任务说明书）
- [ ] 队长集成验收通过（联调无报错）

---

## 11. PR 与合并规则

- 分支命名：`p3/{模块代号}-{姓名}`，如 `p3/insight-zhiming`
- PR 标题：`feat(P3-AI-xxx): 简短描述`
- PR 描述：变更摘要 + 测试截图 + 验收清单勾选
- **不能编辑他人文件**：PR 中不允许出现修改他人独占路径的文件
- 共享文件修改需队长 Review
- 合并顺序：队长基础设施 → 成员模块（任意顺序，无依赖）→ 集成验收

---

## 12. 时间估算

| 阶段 | 时间 | 负责人 | 内容 |
|---|---|---|---|
| 队长基础设施 | 第1周 | 梓源 | AiService + Qdrant + 配置 + 前端组件 + DB脚本 + 说明书 |
| 志明 P3-AI-INSIGHT | 第1-2周 | 志明 | 后端洞察服务 + 前端面板（拿到说明书即可启动） |
| 哲涛 P3-AI-REPORT | 第1-2周 | 哲涛 | 后端数据聚合+AI生成 + 前端预填表单 |
| 许润 P3-AI-SCRIPT | 第1-2周 | 许润 | 前端侧栏（不阻塞）→ 后端向量检索+LLM排序（依赖 Qdrant） |
| 嘉诚 P3-AI-CHURN | 第1-2周 | 嘉诚 | 后端评分引擎+定时任务 + 前端Dashboard卡片 |
| 集成验收 | 第2周末 | 梓源 | 逐个模块联调+Review+合并 |

**总工期：约 2 周**

---

## 13. 即刻行动

1. **梓源**：完成 LEAD-P3-001 ~ 005（AiService + Qdrant + 配置 + 组件 + DB脚本）
2. **梓源**：完成 LEAD-P3-006（4 份个人任务说明书）
3. **全员**：拿到个人任务说明书后，创建分支，启动开发
4. **全员**：每日发进度更新
5. **梓源**：第2周末组织集成验收
