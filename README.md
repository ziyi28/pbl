# 校园活动发布平台 (Campus Activity Platform)

面向高校师生的校园活动发布、管理与互动社交 Web 平台。本项目为 PBL 项目实践的期末答辩成果（第 12 组）。

---

## 🚀 项目定位与痛点解决
* **信息孤岛**：解决校园活动分散在微信群、教务网、公告栏，学生获取难的痛点。
* **报名混乱**：统一问卷、邮件、接龙报名入口，实现名额的实时并发控制与取消机制。
* **互动缺失**：打破单向通知，增加评论、点赞、收藏社交闭环，营造校园活动社交圈。

---

## 🛠️ 技术架构与选型

### 前端 (Presentation Layer)
* **核心框架**：Vue 3 + TypeScript (类型安全，易维护)
* **构建工具**：Vite (极速热重载开发体验)
* **UI 组件库**：Element Plus (响应式企业级组件)
* **状态与路由**：Pinia + Vue Router
* **网络请求**：Axios (支持请求/响应拦截器，自动携带 JWT)

### 后端 (Business Logic Layer)
* **核心框架**：Spring Boot 3.2.5 + Java 17
* **安全框架**：Spring Security (认证与细粒度角色授权)
* **数据持久层**：MyBatis-Plus (ORM 框架，内置乐观锁与软删除)
* **接口文档**：Knife4j (Swagger 3.0，API 自动生成)
* **鉴权机制**：JWT (无状态令牌，安全跨域)

### 数据库 (Data Layer)
* **开发环境**：H2 Database (轻量级内存数据库，零环境依赖)
* **生产环境**：MySQL 8.x (稳定成熟的关系型数据库，含 7 张业务表)

---

## ✨ 核心亮点与创新设计
1. **JWT 安全鉴权与路由守卫**：实现访客（未登录）、普通学生（USER）和管理员（ADMIN）三级权限隔离。
2. **乐观锁高并发报名控制**：采用 MyBatis-Plus 内置乐观锁版本号机制，杜绝多人并发时报名超出名额上限（超卖）问题。
3. **站内消息通知系统**：报名成功、取消报名及活动状态变更自动触发异步站内通知，保证信息直达。
4. **CSV 报名表安全导出**：支持管理员导出参与者名单，代码层面在长日期前追加了制表符防截断，**彻底解决了 Excel 打开时日期压缩折叠成 `###` 井号的经典排版 bug**。

---

## 👥 答辩演示专属账号
本仓库的本地数据库中已预置了第 12 组真实的组员演示数据，**密码统一为 `admin123`**：

| 用户名 | 身份/职责 | 系统权限 | 个人简介 (Bio) |
| :--- | :--- | :--- | :--- |
| `admin` | 系统管理员 | `ADMIN` | 系统超级管理员 |
| `ziyi` | 詹崇权 | `ADMIN` | 第12组项目组长，负责全栈开发与联调 |
| `722906lyl` | 李垚霖 | `USER` | 第12组成员，后端开发 & 测试 |
| `qingchuanone` | 李果霖 | `USER` | 第12组成员，后端开发 & 自动API文档生成 |
| `lihaichuan428` | 李海川 | `USER` | 第12组成员，后端开发 & 问题追踪 |

---

## 📦 快速部署与运行

### 1. 数据库配置
在 MySQL 8.x 中创建数据库 `campus_activity`。
* 数据表结构见：`backend/src/main/resources/db/schema.sql`
* 演示数据脚本见：`backend/src/main/resources/db/init_demo_data.sql`

在 `backend/src/main/resources/application-dev.yml` 修改 MySQL 连接用户名与密码。

### 2. 启动后端 (Spring Boot)
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
启动成功后，可通过 `http://localhost:8080/doc.html` 查看自动生成的 Knife4j 在线 API 接口文档。

### 3. 启动前端 (Vite)
```bash
cd frontend
npm install
npm run dev
```
前端默认运行在 `http://localhost:5173`。
