---
name: yousells-dev
description: Use for any development, planning, coding, testing, refactoring, or release work inside the YouSells project. This skill defines the project's required engineering workflow: clarify requirements first, record development planning and logs, implement module code only after scope is fixed, write automated tests immediately after each module is completed, and explicitly remind developers to perform frontend/browser联调 when a change affects UI or integration behavior.
---

# YouSells Development Skill

Follow this workflow for all work inside `YouSells`.

## Core Rule

Treat `YouSells` as a formal internal software project, not a casual demo.

Every task should follow this order:

1. Clarify the requirement
2. Confirm the development scope
3. Record the plan or change log
4. Implement the module
5. Write automated tests immediately
6. Run verification
7. If the change affects UI, interaction, or integration, explicitly remind the developer to do frontend/browser联调

Do not jump straight into code without first making the requirement and task boundary clear.

## Project Context

- Team size: `5`
- Core stronger developers: `秦梓源`, `志明`
- Team development style: `Vibe Coding`
- Delivery target: internal formal Web platform
- User scale target: `50-100`
- Engineering direction: follow enterprise-style workflow and quality discipline

## Required Workflow

### 1. Requirement First

Before coding, always identify:

- What module is being changed
- What exact problem is being solved
- Whether the task belongs to `P0`, `P1`, or later
- Whether it changes backend only, frontend only, or both
- Whether it changes schema, API contract, permission logic, or deployment behavior

If the scope is not yet explicit, stop and define it in a doc first.

### 2. Planning First

Before implementation, record:

- task objective
- module owner
- affected files or modules
- dependencies
- verification method

Use the project docs under `docs/` to keep planning visible.

Before parallel module work, also align with:

- `docs/00_文档导航.md`
- `docs/02_协作规范与架构边界.md`
- `docs/03_P0任务分配与进度管理.md`

### 3. Development Log

For meaningful tasks, keep a short development log that records:

- date
- owner
- task
- current status
- blockers
- next action

If no dedicated log file exists yet for the task, create or extend one under `docs/logs/`.

### 4. Implementation Discipline

When implementing:

- keep module boundaries clear
- do not mix unrelated changes
- do not bypass agreed conventions
- prefer stable, maintainable code over clever shortcuts
- if changing shared contracts, update docs immediately

For backend work, additionally enforce:

- business modules stay inside their owned directories
- cross-module calls go through `service` interfaces only
- do not place business logic in controllers
- do not add new persistence work without `entity / mapper / convert`
- dashboard aggregation belongs only in `modules/dashboard/**`

### 5. Testing Is Mandatory

After finishing a module, write automated tests immediately.

This is not optional.

At minimum:

- backend module changes must have unit tests
- service logic changes must have correctness tests
- controller or API behavior should have integration-style verification where practical
- utility changes must have focused tests

Do not leave testing as “later work” unless the user explicitly pauses the task.

### 6. 联调 Reminder Rule

If a task affects any of the following, explicitly remind the developer to perform frontend/browser联调:

- UI rendering
- form submission
- table display
- auth flow
- permission behavior
- API contract used by frontend
- any page-level interaction

This reminder should be explicit, not implied.

### 7. Acceptance Mindset

Before considering a task complete, verify:

- requirement is met
- code compiles or runs
- tests pass, or any failure is clearly explained
- docs are updated if behavior changed
- frontend/browser联调 is called out when needed

## Team-Specific Expectations

### For Qin Ziyuan

- own infrastructure, project baseline, engineering rules, and final acceptance
- own architecture direction, repo conventions, CI/testing expectations, and release standards

### For Module Developers

- own their module implementation
- own tests for their module
- own local self-check before asking for acceptance
- do not edit another member's owned directory without lead approval

No developer should hand off a module without tests.

## Task Assignment Quality Standards (队长下发任务规范)

队长向队员下发任务时，任务描述必须达到以下粒度。队员收到的任务卡如果缺关键信息，有权退回要求补充。

### 任务描述必含项

每个任务至少包含以下字段，缺一不可：

| 字段 | 说明 | 示例 |
|---|---|---|
| 目标 | 一句话说清楚要交付什么 | "学生客户表格+多维筛选+新建客户表单" |
| 可编辑路径 | 精确到具体文件或目录，用 `/**` 通配 | `src/components/customer-list/**` |
| 只读参考路径 | 队员可以看但不能改的文件 | `src/utils/request.ts`, `docs/06_*.md` |
| 新建文件清单 | 每个新文件的完整路径 | `src/components/customer-list/CustomerFilterBar.vue` |
| 修改文件清单 | 每个要改的文件的完整路径 | `src/views/customer/CustomerListView.vue` |
| API/数据契约 | 完整的 request body 和 response TypeScript 接口 | 见下方示例 |
| 浏览器/测试验收清单 | 逐项可勾选，按区块分组 | 见下方示例 |
| 前置任务 | 明确的任务 ID 依赖 | `BE-102（客户模型后端）` |
| 联调要求 | 与哪个后端/前端任务联调 | `与 BE-102 联调` |
| 交接说明 | 谁依赖此任务、有什么注意事项 | `嘉诚的 FE-101 依赖此任务` |

### 前端任务粒度标准

前端任务必须包含以下组件级规格，队员不应自己猜测：

1. **每个组件的 Props 表** — 字段名 / 类型 / 是否必传 / 说明
2. **每个组件的 Emits 表** — 事件名 / 参数 / 触发时机
3. **模板结构** — 伪 HTML 含层级嵌套和 `v-if`/`v-else`/`v-for` 分支
4. **Element Plus 组件选型** — 每个表单项指定具体组件（`el-input` / `el-select` / `el-date-picker` / `el-tag` / `el-timeline` / `el-collapse` 等）
5. **状态处理** — 每种状态（loading / empty / error / submitted / readonly / 权限不足）下显示什么
6. **颜色/标签映射** — `el-tag` 的 `type` 属性与业务值的对应关系（如 很稳=success, 可跟=warning）
7. **交互联动逻辑** — 表单字段之间的显隐/默认值/disabled 条件
8. **数据流** — `onMounted` 拉什么、用户操作触发什么、操作成功后刷新什么

**API 数据契约示例（TypeScript 接口格式）：**
```ts
GET /api/customers?page=&pageSize=&keyword=&grade=&progress=&intent=
→ { list: CustomerListItem[]; page: number; pageSize: number; total: number }

CustomerListItem: {
  id: number; realName: string; grade: string; major: string;
  className: string | null; progress: string; intent: string;
  ownerDisplayName: string; inviterDisplayName: string
}
```

**浏览器验收清单示例格式：**
```markdown
**区块名（如"筛选栏区域"）**
- [ ] 具体元素和预期行为
- [ ] 具体元素和预期行为

**区块名（如"表格区域"）**
- [ ] 列 A：el-tag 渲染，type="info"
- [ ] 列 B：null 时显示 "—"
- [ ] 分页条在 total > 0 时出现
```

验收清单必须能逐条打勾，不能写"XXX正常"这种不可判断的表述。

### 后端任务粒度标准

后端任务必须包含：

1. **Entity 字段列表** — 字段名 / Java 类型 / 数据库列名 / 是否 @TableField 映射 / 注释
2. **API 契约** — 路径 + HTTP 方法 + 完整的 request body JSON 示例 + response JSON 示例
3. **Mapper 关键查询** — 哪些查询走 MyBatis XML、哪些用 LambdaQueryWrapper
4. **Service 逻辑要点** — 核心业务规则（如：新增跟进后同步客户最新跟进信息）
5. **测试场景清单** — 正常路径 + 边界 + 异常 + 权限，每个场景的输入和预期输出
6. **数据权限规则** — 本模块是否需要用 DataScopeHelper，怎么用

### 队长下发前自查

下发前逐条确认：

- [ ] 队员拿到这份任务描述，不看任何其他文档，能否画出页面的完整组件树？
- [ ] 队员能否写出所有 Props/Emits 的类型定义？
- [ ] 队员能否知道每个 API 调用的 request body 和 response 结构？
- [ ] 验收清单的每一项是否都可以回答"是/否"而不需要主观判断？
- [ ] 如果队员按验收清单逐条通过，是否就能保证功能完整？

---

## What To Produce During Project Work

Depending on task scope, create or update:

- requirement docs
- planning docs
- task split docs
- development logs
- code
- tests
- verification notes

## Default Behavior

When asked to help with YouSells work:

- first inspect the relevant docs
- then clarify scope
- then update planning/logging artifacts if needed
- then implement
- then test
- then report verification and联调 needs

Do not skip the engineering workflow just because the code change looks small.
