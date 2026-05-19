# YouSells Server

## 运行前说明

这个工程默认使用：

- `JDK 21`
- `Spring Boot 3.x`
- `Session/Cookie` 鉴权基线

当前默认连接本地基础服务：

- `MySQL: localhost:13306`
- `Redis: localhost:16379`

基础服务请优先在项目根目录启动：

```powershell
docker compose up -d
```

详细说明见：

- `../docs/10_本地基础服务启动指南.md`

如果你本机 Maven 全局配置被改成了其他私服或本地 Nexus，建议优先使用项目内配置运行：

```powershell
$env:JAVA_HOME='D:\develop\jdk21'
& 'D:\opt\jdk-11.0.29\apache-maven-3.9.11\bin\mvn.cmd' -s .mvn/settings.xml test
```

如果你要本地启动后端，可优先准备这些环境变量：

```powershell
$env:MYSQL_HOST='localhost'
$env:MYSQL_PORT='13306'
$env:MYSQL_DATABASE='yousells'
$env:MYSQL_USERNAME='root'
$env:MYSQL_PASSWORD='root'
$env:REDIS_HOST='localhost'
$env:REDIS_PORT='16379'
```

## 当前骨架范围

- 登录、当前用户、登出
- 首页看板概览
- 客户、跟进、公共安排、日报周报、话术库接口占位
- 统一响应与统一异常处理
- P0 数据库建表与种子数据 SQL
