# YouSells

`YouSells` 是一个给团队内部使用的 `客户管理 + 协作管理 + 内容沉淀` 平台。

当前项目目标不是做一个大而全的“团队社区”，而是先做一个团队每天真会用的业务中台，优先解决下面几件事：

- 客户统一沉淀
- 跟进记录统一管理
- 负责人和协助关系清晰
- 每日待跟进和公共安排可视化
- 日报周报固定提交
- 话术和经验可沉淀

## 当前项目状态

截至 `2026-05-18`，当前已经明确的方向是：

- 项目名称：`YouSells`
- 目标用户：团队内部成员与管理员
- 目标使用人数：`50-100`
- 项目形态：正式部署上线的内部 Web 平台
- 开发模式：`5 人开发团队`
- 第一阶段重点：先做 `P0`，先把主流程跑通

当前代码层已经落地的基础设施：

- `yousells-server`：Spring Boot 后端骨架、Session/Cookie 鉴权基线、统一响应/异常处理、P0 模块接口占位、基础接口测试
- `yousells-web`：Vue 3 前端骨架、登录页、主布局、路由守卫、认证 store、API 请求层、P0 页面占位与示例数据联动
- `yousells-server/src/main/resources/db`：P0 数据库建表脚本与初始化种子数据
- `docker-compose.yml`：MySQL + Redis 本地开发依赖基线

## 当前定下来的技术方向

- 后端：`JDK 21 + Spring Boot 3.x + Spring Security + MyBatis-Plus`
- 数据库：`MySQL 8`
- 缓存：`Redis`
- 前端：`Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus`
- 部署：`Nginx + Spring Boot 单体 + Docker Compose`
- 鉴权：`v1 使用 Session/Cookie，后续预留 JWT 扩展空间`

## 文档目录

- [项目需求概览](./docs/01_项目需求概览.md)
- [技术选型与部署方案](./docs/02_技术选型与部署方案.md)
- [鉴权与权限模型](./docs/03_鉴权与权限模型.md)
- [开发分工与首期推进](./docs/04_开发分工与首期推进.md)
- [开发流程与日志规范](./docs/05_开发流程与日志规范.md)
- [P0开发任务分配方案](./docs/06_P0开发任务分配方案.md)
- [数据库表设计](./docs/07_数据库表设计.md)
- [API接口基线](./docs/08_API接口基线.md)
- [工程目录初始化方案](./docs/09_工程目录初始化方案.md)
- [本地基础服务启动指南](./docs/10_本地基础服务启动指南.md)

## 当前建议的开发顺序

1. 先定权限模型
2. 再定数据库表设计
3. 再拆前后端页面与接口
4. 再开始正式开发

## 当前代码结构

- `yousells-server/`：后端工程
- `yousells-web/`：前端工程
- `docs/`：需求、规划、规范与日志
- `.codex/skills/yousells-dev/`：项目专属开发规范 skill

## 本地运行提示

- 基础服务统一通过根目录 `docker-compose.yml` 启动，见 [本地基础服务启动指南](./docs/10_本地基础服务启动指南.md)
- 当前项目默认本地端口：
  - `MySQL: 13306`
  - `Redis: 16379`
  - `Backend: 8080`
  - `Frontend: 5173`
- 后端如果被全局 Maven 配置劫持到其他私服，优先使用 `yousells-server/.mvn/settings.xml`
- 前端在 PowerShell 下如遇 `npm.ps1` 执行策略问题，改用 `cmd /c npm ...`

## 当前最重要的原则

- 先做 `P0`
- 先打通主流程
- 先保证能每天真用
- 不一开始做太重
- 为后续扩展留空间，但不提前透支复杂度

## 项目专属 Skill

项目内已经落地专属 skill：

- `.codex/skills/yousells-dev/SKILL.md`

后续在 `YouSells` 目录里进行需求、开发、测试、联调、验收相关工作时，都应按这份 skill 里的流程规范推进。
